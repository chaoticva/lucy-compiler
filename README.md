# Lucy

### currently supported:

- variable assignments
- types: num, str, bool, char, auto, void
- reassignments: num, bool
- builtin functions such as exit() and print()
- functions
- if-else statements
- syntax highlighting for jetbrains IDEs using [this](https://github.com/chaoticva/lucy-language-support) plugin
- Error handling

```lc
var num errorCode = 7 + 3 * (10 / (12 / (3 + 1) - 1)) / (2 + 3) - 5 - 3 + (8); # 10
const var num successCode = 0;

errorCode = 1;

print("Hello, World!");

const var bool error = true;

def void foo() {
    if (error) {
        exit(errorCode);
    } else {
        exit(successCode);
    }
}

foo();
```

### Error Handling

[![dIBnVlR.md.png](https://iili.io/dIBnVlR.md.png)](https://freeimage.host/i/dIBnVlR)<br />
[![dIBB8In.md.png](https://iili.io/dIBB8In.md.png)](https://freeimage.host/i/dIBB8In)

## Note:

this only compiles for x86_64 linux
