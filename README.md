# Crafting Interpreters

This is the repository for my work through the book [Crafting Interpreters](https://craftinginterpreters.com/).

## klox (Kotlin)

This follows the book's implementation of the Lox language in Java, but in Kotlin.

To execute the interpreter use `gradle :klox:installDist && klox/build/install/klox/bin/klox`.

### JVM target

The JVM target is a tree-walking interpreter, like in the book.

#### Testing

`gradle check` runs custom tests defined in this repo, as well as the official language tests.

### JS target

The tree-walking interpreter is available in JS as well.

TODO: But additionally there is a JS backend.