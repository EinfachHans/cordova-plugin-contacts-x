package de.einfachhans.ContactsX;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This class echoes a string called from JavaScript.
 */
public class ContactsX extends CordovaPlugin {

    private CallbackContext _callbackContext;

    public static final String READ = Manifest.permission.READ_CONTACTS;

    public static final int REQ_CODE_READ = 0;
    public static final int REQ_CODE_PICK = 2;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this._callbackContext = callbackContext;

        try {
            if (action.equals("find")) {
                if (PermissionHelper.hasPermission(this, READ)) {
                    this.find(args);
                } else {
                    returnError(ContactsXErrorCodes.PermissionDenied);
                }
            } else if(action.equals("pick")) {
                if (PermissionHelper.hasPermission(this, READ)) {
                    this.pick(args);
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

    public void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        if(requestCode == REQ_CODE_PICK) {
            if(resultCode == Activity.RESULT_OK) {
                String contactId = intent.getData().getLastPathSegment();
                Cursor c =  this.cordova.getActivity().getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                        new String[] {ContactsContract.RawContacts._ID}, ContactsContract.RawContacts.CONTACT_ID + " = " + contactId, null, null);
                if (!c.moveToFirst()) {
                    returnError(ContactsXErrorCodes.UnknownError, "Error occurred while retrieving contact raw id");
                    return;
                }
                String id = c.getString(c.getColumnIndex(ContactsContract.RawContacts._ID));
                c.close();

                JSONObject contact = getContactById(id);
                if(contact != null) {
                    this._callbackContext.success(contact);
                } else {
                    returnError(ContactsXErrorCodes.UnknownError);
                }
            } else {
                returnError(ContactsXErrorCodes.UnknownError);

            }
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        this.hasPermission();
    }

    private void find(JSONArray args) throws JSONException {
        ContactsXFindOptions options = new ContactsXFindOptions(args.optJSONObject(0));

        this.cordova.getThreadPool().execute(() -> {

            ContentResolver contentResolver = this.cordova.getContext().getContentResolver();

            ArrayList<String> projection = this.getProjection(options);
            ArrayList<String> selectionArgs = this.getSelectionArgs(options);
            StringBuilder questionMarks = new StringBuilder();
            for (String s : selectionArgs) {
                if (selectionArgs.indexOf(s) == selectionArgs.size() - 1) {
                    questionMarks.append("?");
                } else {
                    questionMarks.append("?, ");
                }
            }
            String selection = ContactsContract.Data.MIMETYPE + " in (" + questionMarks.toString() + ")";

            Cursor contactsCursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    projection.toArray(new String[0]),
                    selection,
                    selectionArgs.toArray(new String[0]),
                    null
            );

            JSONArray result = null;
            try {
                result = handleFindResult(contactsCursor, options);
            } catch (JSONException e) {
                this.returnError(ContactsXErrorCodes.UnknownError, e.getMessage());
            }

            this._callbackContext.success(result);
        });
    }

    private ArrayList<String> getProjection(ContactsXFindOptions options) {
        ArrayList<String> projection = new ArrayList<>();
        projection.add(ContactsContract.Data.MIMETYPE);
        projection.add(ContactsContract.Contacts._ID);
        projection.add(ContactsContract.Data.CONTACT_ID);
        projection.add(ContactsContract.CommonDataKinds.Contactables.DATA);

        if (options.displayName) {
            projection.add(ContactsContract.Contacts.DISPLAY_NAME);
        }
        if (options.firstName) {
            projection.add(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
        }
        if (options.middleName) {
            projection.add(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME);
        }
        if (options.familyName) {
            projection.add(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);
        }
        if(options.emails) {
            projection.add(ContactsContract.CommonDataKinds.Email._ID);
            projection.add(ContactsContract.CommonDataKinds.Email.DATA);
            projection.add(ContactsContract.CommonDataKinds.Email.TYPE);
            projection.add(ContactsContract.CommonDataKinds.Email.LABEL);
        }

        return projection;
    }

    private ArrayList<String> getSelectionArgs(ContactsXFindOptions options) {
        ArrayList<String> selectionArgs = new ArrayList<>();
        if (options.phoneNumbers) {
            selectionArgs.add(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        }
        if (options.emails) {
            selectionArgs.add(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
        }
        if (options.firstName || options.middleName || options.familyName) {
            selectionArgs.add(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        }

        return selectionArgs;
    }

    private JSONArray handleFindResult(Cursor contactsCursor, ContactsXFindOptions options) throws JSONException {
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
                    if (options.displayName) {
                        String displayName = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        jsContact.put("displayName", displayName);
                    }
                    JSONArray jsPhoneNumbers = new JSONArray();
                    jsContact.put("phoneNumbers", jsPhoneNumbers);

                    JSONArray jsEmails = new JSONArray();
                    jsContact.put("emails", jsEmails);

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
                    case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                        JSONArray emailAddresses = jsContact.getJSONArray("emails");
                        emailAddresses.put(emailQuery(contactsCursor));
                        break;
                    case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
                        try {
                            if (options.firstName) {
                                String firstName = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                                jsContact.put("firstName", firstName);
                            }
                            if (options.middleName) {
                                String middleName = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
                                jsContact.put("middleName", middleName);
                            }
                            if (options.familyName) {
                                String familyName = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                                jsContact.put("familyName", familyName);
                            }
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                }

                contactsById.put(contactId, jsContact);
            }

            contactsCursor.close();
        }

        return jsContacts;
    }

    private JSONObject emailQuery(Cursor cursor) throws JSONException {
        JSONObject email = new JSONObject();
        int typeCode = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.TYPE));
        String typeLabel = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.LABEL));
        String type = (typeCode == ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM) ? typeLabel : getContactType(typeCode);
        email.put("id", cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email._ID)));
        email.put("value", cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA)));
        email.put("type", type);
        return email;
    }

    /**
     * Converts a string from the W3C Contact API to it's Android int value.
     */
    private int getContactType(String string) {
        int type = ContactsContract.CommonDataKinds.Email.TYPE_OTHER;
        if (string != null) {

            String lowerType = string.toLowerCase(Locale.getDefault());

            if ("home".equals(lowerType)) {
                return ContactsContract.CommonDataKinds.Email.TYPE_HOME;
            }
            else if ("work".equals(lowerType)) {
                return ContactsContract.CommonDataKinds.Email.TYPE_WORK;
            }
            else if ("other".equals(lowerType)) {
                return ContactsContract.CommonDataKinds.Email.TYPE_OTHER;
            }
            else if ("mobile".equals(lowerType)) {
                return ContactsContract.CommonDataKinds.Email.TYPE_MOBILE;
            }
            return ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM;
        }
        return type;
    }

    /**
     * getPhoneType converts an Android phone type into a string
     */
    private String getContactType(int type) {
        String stringType;
        switch (type) {
            case ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM:
                stringType = "custom";
                break;
            case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                stringType = "home";
                break;
            case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                stringType = "work";
                break;
            case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
                stringType = "mobile";
                break;
            case ContactsContract.CommonDataKinds.Email.TYPE_OTHER:
            default:
                stringType = "other";
                break;
        }
        return stringType;
    }

    private void pick(JSONArray args) {
        this.cordova.getThreadPool().execute(() -> {
            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            this.cordova.startActivityForResult(this, contactPickerIntent, REQ_CODE_PICK);
        });
    }

    private JSONObject getContactById(String id) {
        Cursor c = this.cordova.getActivity().getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.RAW_CONTACT_ID + " = ? ",
                new String[]{id},
                ContactsContract.Data.RAW_CONTACT_ID + " ASC");

        Map<String, Object> fields = new HashMap<>();
        fields.put("phoneNumbers", true);
        fields.put("emails", true);
        Map<String, Object> pickFields = new HashMap<>();
        pickFields.put("fields", fields);

        try {
            JSONArray contacts = handleFindResult(c, new ContactsXFindOptions(new JSONObject(pickFields)));
            if(contacts.length() == 1) {
                return contacts.getJSONObject(0);
            }
        } catch (Exception e) {
            returnError(ContactsXErrorCodes.UnknownError, e.getMessage());
        }

        return null;
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
