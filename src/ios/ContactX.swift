import Contacts

class ContactX {

    var contact: CNContact;

    init(contact: CNContact) {
        self.contact = contact
    }

    func getJson() -> NSDictionary {

        let phoneNumbers: [String] = self.contact.phoneNumbers.map { (ob: CNLabeledValue<CNPhoneNumber>) -> String in
            return ob.value.stringValue
        }

        return [
            "id": self.contact.identifier,
            "firstName": self.contact.givenName,
            "middleName": self.contact.middleName,
            "familyName": self.contact.familyName,
            "phoneNumbers": phoneNumbers
        ];
    }
}
