var exec = require('cordova/exec');

var contactsX = {
  ErrorCodes: {
    UnsupportedAction: 1,
    WrongJsonObject: 2,
    PermissionDenied: 3,
    UnknownError: 10
  },

  find: function (success, error) {
    exec(success, error, 'ContactsX', 'find', []);
  },

  hasPermission: function (success, error) {
    exec(success, error, 'ContactsX', 'hasPermission', []);
  },

  requestPermission: function (success, error) {
    exec(success, error, 'ContactsX', 'requestPermission', []);
  }
}

module.exports = contactsX;
