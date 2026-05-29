# Style Guidelines

These guidelines generalize the coding style implied by recent refactoring work.

## Structure

- Prefer extracting stateful or decision-heavy logic out of UI/rendering code.
- Prefer splitting files when a single file starts carrying multiple unrelated responsibilities.
- Prefer organizing code around cohesive concepts rather than keeping loosely related types together for convenience.

## APIs

- Prefer naming APIs by caller intent and user-facing behavior, not by internal implementation details or past refactor history.
- Prefer small, named state objects over long parameter lists of primitive values and booleans.
- Prefer APIs that accept intent-level inputs instead of requiring callers to assemble low-level render details.
- Prefer narrowing function responsibilities so each function either computes state or renders it, but does not do both heavily at once.
- Prefer keeping parallel variants symmetric at the same abstraction level.
- Prefer small public APIs and keep implementation details private or internal in focused packages.

## State Modeling

- Prefer explicit state types that make intermediate UI state visible and inspectable.
- Prefer immutable value-style models for derived state where practical.
- Prefer `@JvmInline value class` for single-property state/model wrappers when no multi-property value semantics are needed.
- Prefer naming that matches the domain model and persisted schema consistently across layers.
- Prefer precise names for intermediate concepts; a model name should encode what the value represents, not merely where it is used.
- Avoid wrapper types around existing value types unless the wrapper adds real behavior, invariants, or meaning.

## Control Flow

- Prefer representing true absence with nullability or other explicit optionality instead of sentinel enum values.
- Prefer replacing ad hoc branching in call sites with precomputed decisions in dedicated helpers or model builders.
- Prefer deriving presentation-specific state once, then consuming it declaratively.
- Prefer scoping derived data progressively from broad to narrow instead of repeatedly deriving from the widest input set.
- Prefer removing obsolete extension points, parameters, and state once the use case disappears.

## Readability

- Prefer flatter, easier-to-scan rendering code with minimal inline computation.
- Prefer targeted utility extensions when they simplify repeated conditional patterns without obscuring behavior.
- Prefer code that reads as composition of prepared parts instead of step-by-step assembly mixed with business rules.
- Prefer short, purpose-focused KDoc for generated state or layout models whose role is not immediately obvious.
- Avoid documenting every property when precise names already explain the model.

## Refactoring Direction

- Extract first when a component starts carrying too many local variables or conditional branches.
- Introduce a named type when a group of values travels together or represents a meaningful concept.
- Move logic closer to the model it belongs to when doing so reduces duplication and makes behavior easier to reason about.
