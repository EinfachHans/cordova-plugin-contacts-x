package de.einfachhans.ContactsX;

import android.Manifest;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This class echoes a string called from JavaScript.
 */
public class ContactsX extends CordovaPlugin {

    private CallbackContext _callbackContext;

    public static final String READ = Manifest.permission.READ_CONTACTS;

    public static final int REQ_CODE_READ = 0;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this._callbackContext = callbackContext;

        try {
            if (action.equals("find")) {
                if (PermissionHelper.hasPermission(this, READ)) {
                    this.find();
                } else {
                    returnError(ContactsXErrorCodes.PermissionDenied);
                }
            } else if (action.equals("hasPermission")) {
                this.hasPermission();
            } else if (action.equals("requestPermission")) {
                this.requestPermission(REQ_CODE_READ);
            } else {
                returnError(ContactsXErrorCodes.UnsupportedAction);
            }
        } catch (JSONException exception) {
            returnError(ContactsXErrorCodes.WrongJsonObject);
        } catch (Exception exception) {
            returnError(ContactsXErrorCodes.UnknownError, exception.getMessage());
        }

        return true;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        this.hasPermission();
    }

    private void find() throws JSONException {
        this.cordova.getThreadPool().execute(() -> {

            ContentResolver contentResolver = this.cordova.getContext().getContentResolver();

            String[] projection = new String[]{
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.Contacts._ID,
                    ContactsContract.Data.CONTACT_ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                    ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
                    ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                    ContactsContract.CommonDataKinds.Contactables.DATA,
            };
            String selection = ContactsContract.Data.MIMETYPE + " in (?, ?)";
            String[] selectionArgs = new String[]{
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
            };

            Cursor contactsCursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );

            JSONArray result = null;
            try {
                result = handleFindResult(contactsCursor);
            } catch (JSONException e) {
                this.returnError(ContactsXErrorCodes.UnknownError, e.getMessage());
            }

            if (contactsCursor != null) {
                contactsCursor.close();
            }

            this._callbackContext.success(result);
        });
    }

    private JSONArray handleFindResult(Cursor contactsCursor) throws JSONException {
        // initialize array
        JSONArray jsContacts = new JSONArray();

        if (contactsCursor != null && contactsCursor.getCount() > 0) {
            HashMap<Object, JSONObject> contactsById = new HashMap<>();

            while (contactsCursor.moveToNext()) {
                String contactId = contactsCursor.getString(
                        contactsCursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
                );

                JSONObject jsContact = new JSONObject();

                if (!contactsById.containsKey(contactId)) {
                    // this contact does not yet exist in HashMap,
                    // so put it to the HashMap

                    jsContact.put("id", contactId);
                    String displayName = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    jsContact.put("displayName", displayName);
                    JSONArray jsPhoneNumbers = new JSONArray();
                    jsContact.put("phoneNumbers", jsPhoneNumbers);

                    jsContacts.put(jsContact);
                } else {
                    jsContact = contactsById.get(contactId);
                }

                String mimeType = contactsCursor.getString(
                        contactsCursor.getColumnIndex(ContactsContract.Data.MIMETYPE)
                );
                String data = contactsCursor.getString(
                        contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.DATA)
                );

                assert jsContact != null;
                switch (mimeType) {
                    case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                        JSONArray jsPhoneNumbers = jsContact.getJSONArray("phoneNumbers");
                        jsPhoneNumbers.put(data);
                        break;
                    case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
                        try {
                            String firstName = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                            String middleName = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
                            String familyName = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                            jsContact.put("firstName", firstName);
                            jsContact.put("middleName", middleName);
                            jsContact.put("familyName", familyName);
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                }

                contactsById.put(contactId, jsContact);
            }
        }
        return jsContacts;
    }

    private void hasPermission() throws JSONException {
        JSONObject response = new JSONObject();
        response.put("read", PermissionHelper.hasPermission(this, READ));
        if (this._callbackContext != null) {
            this._callbackContext.success(response);
        }
    }

    private void requestPermission(int requestCode) {
        PermissionHelper.requestPermission(this, requestCode, READ);
    }

    private void returnError(ContactsXErrorCodes errorCode) {
        returnError(errorCode, null);
    }

    private void returnError(ContactsXErrorCodes errorCode, String message) {
        if (_callbackContext != null) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("code", errorCode.value);
            resultMap.put("message", message == null ? "" : message);
            _callbackContext.error(new JSONObject(resultMap));
            _callbackContext = null;
        }
    }
}
