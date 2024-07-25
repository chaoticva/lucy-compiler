# Lucy

### currently supported:

- variable assignments
- types: number, string, bool
- reassignments: number, bool
- builtin functions such as exit() and print()
- functions
- if-else statements
- syntax highlighting for jetbrains IDEs using [this](https://github.com/chaoticva/lucy-language-support) plugin

```lc
var errorCode = 7 + 3 * (10 / (12 / (3 + 1) - 1)) / (2 + 3) - 5 - 3 + (8); # 10
var successCode = 0;

errorCode = 1;

print("Hello, World!");

var error = true;

def foo() {
  if (error) {
    exit(errorCode);
  } else {
    exit(successCode);
  }
}

foo();
```

## Note:

this only compiles for x86_64 linux
