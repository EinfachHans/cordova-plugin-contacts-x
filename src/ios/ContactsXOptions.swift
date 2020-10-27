class ContactsXOptions {

    var firstName: Bool = true;
    var middleName: Bool = true;
    var familyName: Bool = true;
    var phoneNumbers: Bool = false;
    var emails: Bool = false;

    init(options: NSDictionary?) {
        if(options != nil) {
            let fields = options?.value(forKey: "fields") as? NSDictionary ?? nil;

            if(fields != nil) {
                self.parseFields(fields: fields!)
            }
        }
    }

    private func parseFields(fields: NSDictionary) {
        firstName = fields.value(forKey: "firstName") as? Bool ?? true;
        middleName = fields.value(forKey: "middleName") as? Bool ?? true;
        familyName = fields.value(forKey: "familyName") as? Bool ?? true;
        phoneNumbers = fields.value(forKey: "phoneNumbers") as? Bool ?? false;
        emails = fields.value(forKey: "emails") as? Bool ?? false;
    }

}
