# NoteCrypt Frequently Asked Questions(FAQs).
This is a compilation of Frequently Asked Questions for NoteCrypt. This document will aid prospective and existing end-users of the software as a self-service guide in cases where the developer can't be reached. This document is subject to change and modifications at any time. Thus it'll be updated from time to time consequently to suit any new changes to the software as new developments would be implemented which might cause a discrepancy with this current document.

**What is NoteCrypt?**
- NoteCrypt is an Android software that helps you secure your supposedly private notes in an encrypted database.

**What are the features of NoteCrypt?**

NoteCrypt has quite a handful of features they include;

- Multiple database creation with different passwords.
- Create a new note.
- Tag filter
- Favorite note
- Note sorting
- The database is securely encrypted with AES 128 bit, using cipher-block chaining (CBC), PKCS5Padding and a random IV generated at every save. For the KDF a random salt and PBKDF2WithHmacSHA1 are used.
- Auto create a hidden backup database to prevent data loss in case the file gets corrupted while saving (the backup file is saved with the name: ."database name").
- Note search.

**What is the minimum length of characters to set a password?**

- The minimum password for a database is one character, however, it's not advisable to secure a database with just a single digit or alphabet. NoteCrypt allows numeric and alphabetic password entry. For more security, create a password mixed with alphabets and numbers at least 8 characters.

**Can I change my database password at a later time?**

- Yes, you can change the password of a database at any time. This is one of the features of NoteCrypt. To change the database password, 

- first, unlock the database, 
- click the 3 dots by the top right of the app, 
- select the option to change the password,
- input new password, confirm password and click OK.

**Can I delete a database?**

- Yes, an unwanted database can be deleted. All notes contained in that database will be lost.

**What location is a database saved in my device storage?**

- Every database created is automatically saved to a location "NoteCrypt" on your local device storage. 

**Can I share my notes to other locations and mediums?**

Yes, notes in a database can be shared to other locations or mediums. To share a note,

- unlock the database
- open note in the database you wish to send or share
- click the 3 dots on the top right of the app
- select share option and choose where you wish to share the note to.

**I have note files in my device storage, can I import them into a database?**

- No, you can't import a note inside a database. You can only create a new note and copy text from the note you wish to import and paste it in the newly created note on NoteCrypt

**Important note is a feature of NoteCrypt, how can I mark a note as important?**

To mark a note as important or favorite note,
- unlock the database where the note is contained
- open the note 
- click the star icon on the top of the app and the note is now marked as important/favorite

**I like to format my texts, does NoteCrypt support markdown text styling?**

- No, NoteCrypt does not support markdown text styling, but the formats can be inputted when creating a note and you can preview with on a platform that supports markdown.

**What's the purpose of tags?**

- With tags, you can quickly access notes that carry that tag from the tag filter. For example, you tag a note with software. When you click the tag filter and select software, it brings out all the notes that have the software tag.

**Can I make a manual backup of my database?** 

- Yes, you can make a manual backup of a note database. The database is stored in a location on your device storage named ```NoteCrypt```. From that folder, you can see all your database file and can copy them to another location or send to your cloud storage.

**I'm changing my phone, can I transfer my notes?**

- Yes, it is possible to transfer notes to another device by sharing the file to the new device. 

**I forgot my password, can I recover my notes?** 

- No, lost passwords cannot be recovered. It's advisable to use passwords that you can always remember.

**Can you or someone else read my notes?** 

- No, they are only locally stored and encrypted with the chosen password except your password is compromised or given out.

**Can I read my notes on my PC or on iOS?** 

- No, this is available to only Android devices.

**Are there locked functionalities I need to pay for before use?**

- No, NoteCrypt is an open source software that is totally free and contains no unnecessary ads and in-app purchases.

**I'm skeptical about my privacy, does the developer have access to and collects any information externally when I utilize NoteCrypt?**

- No, the developer has no means of collecting your information when the software is utilized. Read [NoteCrypt Privacy policy](https://github.com/RyuzakiKK/NoteCrypt/blob/master/Privacy.md)

**I experienced a crash, how can and where do I send a crash report?

If you experienced a crash, you can send a detailed crash report to the developer via an [issue](https://github.com/RyuzakiKK/NoteCrypt/issues/new) on Github, preferably with a logcat, to enable the developer to pinpoint the cause of the crash and fix it accordingly.

**I need to make further inquiries, how can I contact the developer?**
For more inquiries, contact the developer via an [issue](https://github.com/RyuzakiKK/NoteCrypt/issues/new) on Github.
