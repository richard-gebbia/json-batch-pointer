# JSON Batch Pointer

Like [JSON Pointer](https://tools.ietf.org/html/rfc6901), except that it:

* can be used to refer to multiple items in a JSON document at once
* is JSON itself, and can therefore be embedded in JSON documents or sent via the `application/json` content type

## Example Usage

Given the following JSON document:

```json
```

and this corresponding JSON batch pointer:

```json
```

one can expect the following extraction:

```json
```

## Distribution

* Clojure (1.8 and above)
    TODO
* Java/Maven
    TODO