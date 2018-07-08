# JSON Batch Pointer

Like [JSON Pointer](https://tools.ietf.org/html/rfc6901), except that it:

* can be used to refer to multiple items in a JSON document at once
* is JSON itself, and can therefore be embedded in JSON documents or sent via the `application/json` content type

## Example Usage

Given the following JSON document:

```javascript
{
    "foo": 23,
    "bar": "hello",
    "baz": {
        "quux": true,
        "bang": [7, "wow", {"yello": "dello"}],
        "pow": [
            {
                "a": 3,
                "b": "yes"
            },
            {
                "a": 5,
                // notice: no "b"
                "c": "no"
            }
        ]
    }
}
```

and this corresponding JSON batch pointer:

```javascript
[
    "foo",  // select "foo" from the root document
    {
        "baz": [
            "quux",  // like JSON Pointer "/baz/quux"
            {
                // grab indexes 1 and 2 out of an array, also the length of the array
                "bang": [1, "2", "length"],

                // grab items "a" and "b" out of each element in this array
                "pow": [["a", "b"]]
            }
        ]
    },
    "nonexistent"   // target document doesn't have this key
]
```

one can expect the following extraction:

```javascript
{
    "foo": 23,
    "baz": {
        "quux": true,

        // "bang" is treated as though it was an object rather than an array
        "bang": {
            "1": "wow",
            "2": {"yello": "dello"},
            "length": 3
        },
        "pow": [
            {
                "a": 3,
                "b": "yes"
            },
            {
                "a": 5
                // no "b" here because it wasn't present in the target document
            }
        ]
    }
    // no "nonexistent" here because it wasn't present in the target document
}
```

## Documentation

More detailed, specification-like documentation TODO.

## Distribution

* Clojure (1.8 and above)
    TODO
* Java/Maven
    TODO