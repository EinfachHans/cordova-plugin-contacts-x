var plugin = function () {
  return window.ContactsX || {};
};
var ContactsX = /** @class */ (function () {
  function ContactsX() {
  }

  ContactsX.ErrorCodes = plugin().ErrorCodes;

  ContactsX.find = function (success, failure, options) {
    var plu = plugin();
    return plu.find.apply(plu, arguments);
  };

  ContactsX.pick = function (success, failure) {
    var plu = plugin();
    return plu.pick.apply(plu, arguments);
  };

  ContactsX.hasPermission = function (success, failure) {
    var plu = plugin();
    return plu.hasPermission.apply(plu, arguments);
  };

  ContactsX.requestPermission = function (success, failure) {
    var plu = plugin();
    return plu.requestPermission.apply(plu, arguments);
  };

  return ContactsX;
}());
export default ContactsX;
