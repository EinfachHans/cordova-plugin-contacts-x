package de.einfachhans.ContactsX;

import org.json.JSONObject;

public class ContactsXFindOptions {
    
    boolean displayName = true;
    boolean firstName = true;
    boolean middleName = true;
    boolean familyName = true;
    boolean organizationName = true;
    boolean phoneNumbers;
    boolean emails;
    String baseCountryCode = null;

    public ContactsXFindOptions(JSONObject options) {
        if (options != null) {
            JSONObject fields = options.optJSONObject("fields");

            if(fields != null) {
                this.parseFields(fields);
            }

            String baseCountryCode = options.optString("baseCountryCode", null);

            if(baseCountryCode != null){
                this.baseCountryCode = baseCountryCode;
            }
        }
    }

    private void parseFields(JSONObject fields) {
        this.displayName = fields.optBoolean("displayName", true);
        this.firstName = fields.optBoolean("firstName", true);
        this.middleName = fields.optBoolean("middleName", true);
        this.familyName = fields.optBoolean("familyName", true);
        this.organizationName = fields.optBoolean("organizationName", true);
        this.phoneNumbers = fields.optBoolean("phoneNumbers");
        this.emails = fields.optBoolean("emails");
    }
}
