declare module 'cordova-plugin-contacts-x' {

  interface ContactX {
    id: string;

    /**
     * android only
     */
    displayName: string;

    /**
     * first name (given name) of the contact
     */
    firstName: string;

    /**
     * middle name of the contact
     */
    middleName: string;

    /**
     * family name of the contact
     */
    familyName: string;


    /**
     * unformatted phone-numbers of the contact
     */
    phoneNumbers: string[];
  }
}
