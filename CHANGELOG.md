# Changelog

## unreleased
- `rawId` added to contact object on android
- `pick` Method added
- `save` Method added
- `delete` Method added
- `requestWritePermission` Method added (android only)
- `hasPermission` now also return result for `write` permission (same like read on iOS)

### Breaking Changes:

- Phonenumber's now returns an array of objects (see [here](readme.md#contactxphonenumber))

## 1.1.0
- Fields can be specified (**Breaking**: phoneNumbers disabled by default)
- emails field added 

## 1.0.0
- Initial Release
