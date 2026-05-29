# Kotlin Modeling Guidelines

## KDoc Style

- Document purpose and responsibility, not implementation mechanics.
- Prefer short KDoc on public model types that explains what the type owns, what it intentionally does not own, and how it relates to persistence or UI boundaries.
- Avoid restating property names in prose unless the relationship between properties is non-obvious.
- Call out stability and persistence contracts explicitly, especially for ids, ordinals, serialized values, and migration-sensitive types.
- Keep method KDoc focused on behavioral guarantees, invariants, and non-obvious side effects. Do not document obvious getters, simple `copy` helpers, or self-explanatory one-liners.
- When a class exists to separate responsibilities, name those responsibilities directly in the KDoc so future changes do not collapse the separation again.

## Inline Classes

- Prefer `@JvmInline value class` for small domain wrappers around primitive or platform types whenever it improves type safety without adding meaningful runtime or API complexity.
- Use inline classes for ids, serialized strings, uris, ordinals, counts, and other values where mixing two values of the same primitive type would be a realistic bug.
- Do not use an inline class when the value needs multiple fields, inheritance, mutable state, or when it would make calling code materially noisier without preventing likely mistakes.
- Keep inline classes behavior-light. They may expose validation, parsing, formatting, or narrow convenience accessors, but should not grow into service objects.
