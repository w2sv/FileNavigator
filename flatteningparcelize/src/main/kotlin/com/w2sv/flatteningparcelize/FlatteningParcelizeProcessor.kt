package com.w2sv.flatteningparcelize

import android.os.Parcel
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toClassName

@KspExperimental
class FlatteningParcelizeProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(FlatteningParcelize::class.qualifiedName!!)

        val unprocessedSymbols = mutableListOf<KSAnnotated>()

        symbols.forEach { symbol ->
            if (symbol is KSClassDeclaration) {
                try {
                    generateParcelable(symbol)
                } catch (e: Exception) {
                    logger.error("Error processing class ${symbol.simpleName.asString()}: ${e.message}", symbol)
                    unprocessedSymbols.add(symbol)
                }
            } else {
                unprocessedSymbols.add(symbol)
            }
        }

        return unprocessedSymbols
    }

    private fun generateParcelable(originalClass: KSClassDeclaration) {
        val originalClassName = originalClass.simpleName.asString()
        val packageName = originalClass.packageName.asString()
        val className = originalClass.toClassName()

        // Separate delegated properties and owned properties
        val delegateProps = mutableListOf<KSPropertyDeclaration>()
        val ownProps = mutableListOf<KSPropertyDeclaration>()

        // Iterate over properties in the class
        originalClass.getDeclaredProperties().forEach { prop ->
            val type = prop.type.resolve()

            // If this property is delegated, we can detect it using its type or other indicators
            // Assuming delegated properties have the `by` keyword, we check if the type is another class or delegate type
            if (type.declaration is KSClassDeclaration && prop.annotations.any { it.shortName.asString() == "delegate" }) {
                delegateProps.add(prop)
            } else {
                ownProps.add(prop)
            }
        }

        // Generate the parcelable code
        val parcelableCode = generateParcelableMethods(className, delegateProps, ownProps)

        // Create the file using KotlinPoet
        val fileSpec = FileSpec.builder(packageName, "${originalClassName}Parcelable")
            .addFunction(parcelableCode)
            .build()

        val file = codeGenerator.createNewFile(
            Dependencies(false, originalClass.containingFile!!),
            packageName,
            "${originalClassName}Parcelable"
        )

        file.bufferedWriter().use { fileSpec.writeTo(it) }
    }

    private fun generateParcelableMethods(
        className: ClassName,
        delegateProps: List<KSPropertyDeclaration>,
        ownProps: List<KSPropertyDeclaration>
    ): FunSpec {
        // `writeToParcel()` method
        val writeToParcelCode = CodeBlock
            .builder()
            .addStatement("override fun writeToParcel(parcel: Parcel, flags: Int) {")
            .apply {
                delegateProps.forEach {
                    val propName = it.simpleName.asString()
                    addStatement("  parcel.write${it.type.resolve()}(this.$propName)")
                }
                ownProps.forEach {
                    val propName = it.simpleName.asString()
                    addStatement("  parcel.write${it.type.resolve()}(this.$propName)")
                }
                addStatement("}")
            }
            .build()

        // `describeContents()` method
        val describeContentsCode = CodeBlock.builder()
            .addStatement("override fun describeContents(): Int = 0")
            .build()

        // `CREATOR` companion object
        val creatorCode = CodeBlock.builder()
            .addStatement("companion object CREATOR : Parcelable.Creator<$className> {")
            .addStatement("  override fun createFromParcel(parcel: Parcel): $className {")
            .addStatement("    val delegate = ${delegateProps[0].type.resolve()}(/* extract data from parcel here */)")
            .addStatement("    return $className(delegate /* other properties from parcel */)")
            .addStatement("  }")
            .addStatement("  override fun newArray(size: Int): Array<$className?> = arrayOfNulls(size)")
            .addStatement("}")
            .build()

        return FunSpec.builder("writeToParcel")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("parcel", Parcel::class)
            .addParameter("flags", Int::class)
            .addCode(writeToParcelCode)
            .addCode(describeContentsCode)
            .addCode(creatorCode)
            .build()
    }
}
