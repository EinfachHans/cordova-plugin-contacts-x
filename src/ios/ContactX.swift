import Contacts

class ContactX {

    var contact: CNContact;
    var options: ContactsXOptions;

    init(contact: CNContact, options: ContactsXOptions) {
        self.contact = contact
        self.options = options;
    }

    func getEmailAddresses() -> [NSDictionary] {
        let labeledValues: [NSDictionary] = self.contact.emailAddresses.map { (ob: CNLabeledValue<NSString>) -> NSDictionary in
            return [
                "id": ob.identifier,
                "type": CNLabeledValue<NSString>.localizedString(forLabel: ob.label ?? ""),
                "value": ob.value
            ]
        }
        return labeledValues;
    }

    func getJson() -> NSDictionary {

        var phoneNumbers: [String] = [];
        if(options.phoneNumbers) {
            phoneNumbers = self.contact.phoneNumbers.map { (ob: CNLabeledValue<CNPhoneNumber>) -> String in
                return ob.value.stringValue
            }
        }

        var emails: [NSDictionary] = [];
        if(options.emails) {
            emails = self.getEmailAddresses();
        }

        var result: [String : Any] = [
            "id": self.contact.identifier,
            "phoneNumbers": phoneNumbers,
            "emails": emails
        ];

        if(options.firstName) {
            result["firstName"] = self.contact.givenName;
        }
        if(options.middleName) {
            result["middleName"] = self.contact.middleName;
        }
        if(options.familyName) {
            result["familyName"] = self.contact.familyName;
        }

        return result as NSDictionary;
    }
}
