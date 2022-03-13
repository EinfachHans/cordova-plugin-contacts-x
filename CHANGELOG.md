### [2.1.1](https://github.com/EinfachHans/cordova-plugin-contacts-x/compare/V2.1.0...V2.1.1) (2022-03-13)


### Bug Fixes

* webview no longer gets removed ([#25](https://github.com/EinfachHans/cordova-plugin-contacts-x/issues/25)) ([b5aefa8](https://github.com/EinfachHans/cordova-plugin-contacts-x/commit/b5aefa8df94510e5ca0e3b2a03df8c9b842a89b6))

## [2.1.0](https://github.com/EinfachHans/cordova-plugin-contacts-x/compare/V2.0.3...V2.1.0) (2022-02-17)


### Features

* add baseCountryCode option for normalizing phonenumbers ([#21](https://github.com/EinfachHans/cordova-plugin-contacts-x/issues/21)) ([36e50ff](https://github.com/EinfachHans/cordova-plugin-contacts-x/commit/36e50ff3ecf80f663259d31d1a8601ea50551212))
* Add support for organizationName ([#19](https://github.com/EinfachHans/cordova-plugin-contacts-x/issues/19)) ([d6e45c4](https://github.com/EinfachHans/cordova-plugin-contacts-x/commit/d6e45c40c1b7d85a26d4269b8e7fe65e27696d5c))

# 2.0.3
- Rename definition File to work on case-sensitive file system [#11](https://github.com/EinfachHans/cordova-plugin-contacts-x/pull/11)

# 2.0.2
- Fix typo

# 2.0.1
- Fix iOS Phone Type (closes [#9](https://github.com/EinfachHans/cordova-plugin-contacts-x/issues/9))

## 2.0.0
- `rawId` added to contact object on android
- `pick` Method added
- `save` Method added
- `delete` Method added
- `requestWritePermission` Method added (android only)
- `hasPermission` now also return result for `write` permission (same like read on iOS)

### Breaking Changes:

- PhoneNumber's now returns an array of objects (see [here](readme.md#contactxphonenumber))

## 1.1.0
- Fields can be specified (**Breaking**: phoneNumbers disabled by default)
- emails field added 

## 1.0.0
- Initial Release
