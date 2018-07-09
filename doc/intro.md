# JavaScript Object Notation (JSON) Batch Pointer

## Abstract

JSON Batch Pointer defines a syntax that itself conforms to JavaScript Object Notation (JSON) for identifying multiple values within a JSON document. The goal of JSON batch pointer is to have a competitor to [GraphQL](https://graphql.org/) queries (not mutations) that:
- is JSON (can be sent with the `application/json` content-type and can be parsed/serialized/manipulated using the numerous available JSON libraries)
- maps a request to data rather than to an API
- does not have a type system, lowering the scope and barrier to entry, and allowing it to more easily be used by programming languages with no or weak type systems

A valid JSON Batch pointer must be a JSON array. Each item in the array must be either:
- a JSON string,
- a JSON number,
- a JSON object, or
- a JSON array

If the item is a string, it refers to a key in the root level of the target document. If the target document represents an array, valid string items in the JSON batch pointer represent indexes into the array as well as the "length" key to get the number of items in the target array. In this way, arrays are treated like objects.

Examples:
- Target document: `{"foo": "bar"}`
- JSON batch pointer: `["foo"]`
- Should retrieve: `{"foo": "bar"}`

- Target document: `["hello", "goodbye", -17]`
- JSON batch pointer: `["0", "2"]`
- Should retrieve: `{ "0":  "hello", "2": -17}`

If the item is a number, it is only valid if the target document is an array. The number is an index into the array.

Examples:
- Target document: `["hello", "goodbye", -17]`
- JSON batch pointer: `[0, 2]`
- Should retrieve: `{ "0": "hello", "2": -17}`

If the item is an object, it is only valid if the target document is also an object and its keys are intended to match keys in the target document. The values of the item must themselves be JSON batch pointers, with their target documents being the values of the corresponding keys in the item's target document.

Exmples:
- Target document
```json
{
    "foo": 3,
    "bar": {
        "baz": 2,
        "quux": "hello"
    },
    "a": [
        {
            "b": 3,
            "c": "wow"
        },
        {
            "b": 12,
            "c": "something"
        }
    ]
}
```

- JSON batch pointer:
```json
[
    "foo",
    {
        "bar": ["baz"],
        "a": [
            {
                "0": ["b"]
            }
        ]
    }
]
```

- Should retrieve:
```json
{
    "foo": 3,
    "bar": {
        "baz": 2
    },
    "a": {
        "0": {
            "b": 3
        }
    }
}
```

If the item is an array, it is only valid if the target document is also an array. The array should have exactly one item in it, and that item should, itself, be a JSON batch pointer. The intent of this is to retrieve a set of properties from each item as an object in the target document (which should be an array).

Examples:
- Target document
```json
[
    {
        "foo": 3,
        "bar": "hi"
    },
    {
        "foo": 4,
        "bar": "bye",
        "baz": true
    }
]
```
- JSON batch pointer: `[["foo", "bar"]]`
- Should retrieve:
```json
[
    {
        "foo": 3,
        "bar": "hi"
    },
    {
        "foo": 4,
        "bar": "bye"
    }
]
```

Documentation TODO:
* unfound keys
