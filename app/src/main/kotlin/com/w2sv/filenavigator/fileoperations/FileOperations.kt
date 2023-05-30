package com.w2sv.filenavigator.fileoperations

import java.io.IOException
import java.nio.file.CopyOption
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.spi.FileSystemProvider

@Throws(IOException::class)
fun Path.moveTo(target: Path, vararg options: CopyOption) {
    val provider = getProvider()
    if (provider == target.getProvider()) {
        provider.move(this, target, *options)
    }
}

fun Path.getProvider(): FileSystemProvider = fileSystem.provider()

@Throws(IOException::class)
fun moveAtomically(source: Path, target: Path) {
    source.moveTo(target, LinkOption.NOFOLLOW_LINKS, StandardCopyOption.ATOMIC_MOVE)
}