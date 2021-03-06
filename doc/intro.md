# JavaScript Object Notation (JSON) Batch Pointer

## Objective

JSON Batch Pointer defines a syntax that itself conforms to JavaScript Object Notation (JSON) for identifying multiple values within a JSON document. The goal of JSON batch pointer is to have a competitor to [GraphQL](https://graphql.org/) queries (not mutations) that:
- is JSON (can be sent with the `application/json` content-type, can be parsed/serialized/manipulated using the numerous available JSON libraries, can be embedded in other JSON documents)
- maps a request to data rather than to an API
- does not have a type system, lowering the scope and barrier to entry, and allowing it to more easily be used by programming languages with no or weak type systems

## Specification

A valid JSON Batch pointer must be a JSON array. Each item in the array must be either:
- a JSON string,
- a JSON number (integers only),
- a JSON object, or
- a JSON array

### String selectors

If the item is a string, it refers to a key in the root level of the target document. If the target document represents an array, valid string items in the JSON batch pointer include the "length" key to get the number of items in the target array. If the string can be parsed into a number and the target document is an array, it is treated as though it is a number selector (explained below).

Also if the target document is an array, the special string selector `"-"` can be used to point to the last element in the array. This behavior exists to mimic [JSON Patch](http://jsonpatch.com/#json-pointer).

#### Examples
1. 
  - Target document: `{ "foo": "bar" }`
  - JSON batch pointer: `["foo"]`
  - Should retrieve: `{ "foo": "bar" }`
2.
  - Target document: `["hello", "goodbye", -17, false]`
  - JSON batch pointer: `["0", "2", "length", "-"]`
  - Should retrieve: `{ "0":  "hello", "2": -17, "length": 4, "-": false }`

### Number selectors

If the item is a number, it is only valid if it is an integer and the target document is an array. The number is an index into the array. The number can be negative, in which case it indexes into the end of the array. A value of -1 points to the last element in the target array, a value of -2 points to the second-to-last element in the target array, etc.

#### Example
1.
- Target document: `["hello", "goodbye", -17]`
- JSON batch pointer: `[0, 2]`
- Should retrieve: `{ "0": "hello", "2": -17 }`
2.
- Target document: `["foo", true, 50, { "hi": "bye" }]`
- JSON batch pointer: `[1, -1]`
- Should retrieve: `{ "1": true, "-1": { "hi": "bye" }}`

### Object selectors

If the item is an object, it is only valid if the target document is also an object and its keys are intended to match keys in the target document. The values of the item must themselves be JSON batch pointers, with their target documents being the values of the corresponding keys in the item's target document.

#### Example
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

### Array selectors

If the item is an array, it is only valid if the target document is also an array. The array should have exactly one item in it, and that item should, itself, be a JSON batch pointer. The intent of this is to retrieve a set of properties from each item as an object in the target document (which should be an array).

#### Example
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

### Unfound Keys

If a key requested in the target document by the JSON batch pointer does not exist, it is simply left out of the retrieved value. This behavior was chosen to differentiate between an unfound value and, say, a value that was set to `null`.

#### Examples
1.
- Target document: `{ "foo": 3 }`
- JSON batch pointer: `["bar"]`
- Should retrieve: `{}`
2.
- Target document: `{ "foo": { "bar": 3 } }`
- JSON batch pointer: `[{ "foo": ["baz"] }]`
- Should retrieve: `{ "foo": {} }`
