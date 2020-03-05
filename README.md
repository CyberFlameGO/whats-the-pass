# Whats The Pass

**Whats The Pass** is a **command-line** application that allows one to encrypt some secret data using secret questions, and then decrypt it using answers to those questions. **It is not a standalone password manager!** However, you can use Whats The Pass if you are afraid to forget/lose your password manager master password.

> **Example usage:** let's say you are using a password manager like 1Password or Dashlane to store all your passwords and other private data. In order to access that data, you only need one password — the so-called **master password**. In case you think you can forget it, you can use **Whats The Pass**.


# How does it work

**Whats The Pass** allows you to encrypt any secret data (including, say, your password manager master password as in the example above) using **secret questions**, i.e. questions **no one knows answers for, except for you**. It is simple:

1. You create a list of very personal/difficult questions that **only you can answer**.
2. You answer those questions.
3. You enter the data you want to encrypt (password manager master password in our example).
4. **Whats The Pass** encrypts the data you entered with [AES-256](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard), using the secret answers as keys and generates your **recovery** file (ending with `.wtp.json`).
5. Now if you'll ever forget your password, you can just run **Whats The Pass**, select the **recovery file** created in **step 4**, and answer the secret questions created in **step 1**. If you answer everything correctly, **Whats The Pass** will decrypt and show you your forgotten data.


# Important notices

1. Make sure no one, except for you, can answer the questions you create, otherwise your password (or other secret data that you want to save using **Whats The Pass**) may be compromised.
2. **Whats The Pass** does not save **answers** to the questions you create. The recovery file only stores the questions themselves. That is, if you forget an answer to any of questions you created, you will **lose your data without a chance to restore it**. Make sure you can always remember how to answer all the questions you enter!
3. Always keep the recovery file **(see step 4)** in a safe place. If you lose it and forget your data (password), you will have no chance to restore it!
4. Even if the recovery file falls into bad hands, if the questions you created are truly secure (and only **you** can answer them), then no one will be able to access your data since it is well encrypted.


# Downloading and running

You can always download the latest version of **Whats The Pass** from **[the Releases page](https://github.com/MeGysssTaa/whats-the-pass/releases)**.

> If your Java version is older than **8u161**, then you also have to install **[Java Cryptograhpy Extension](https://www.oracle.com/java/technologies/javase-jce-all-downloads.html)**.

Note that **What The Pass** is a **command-line (console)** application. That is, it has no graphical interface. In order to run it, open your terminal/cmd and type `java -jar WhatsThePass.jar`

That's it!

![](https://blob.sx/YSPr)


# Creating a recovery file

In order to create a recovery file that you can use later to restore your password, type `1` in the window you've just opened and hit `ENTER`.

You will be asked for the number of secret questions you'd like to use for encryption. It is up to you to decide how many questions do you want, but it's strongly recommended that you use at least 5. Then just enter the questions themselves and answers to them one by one. 

After that, you will need to enter the data to encrypt (in our example, it's the password manager master password, but actually it may be any data you'd like to save securely). 

Then you will be asked to type **extra** comments. Those comments will be shown to you when you will be recovering data using this file. It may be some hints useful **for you (and only for you)** to answer the questions correctly. This step is optional, feel free to skip it by simply hitting `ENTER` without typing anything.
 
Finally, you will be asked how would you like to name your recovery file. It is adviced that you give it a meaningful name so that you can easily find it later. After that a file with the specified name will be created in the folder where `WhatsThePass.jar` is, with extension `.wtp.json`. **That is your recovery file!**

![](https://blob.sx/ye6P)

# Restoring data using recovery file

Now let's say you've forgotten your password. In order to recover it, run **Whats The Pass** and enter `2` in the window appeared.

You will be asked where the **recovery file** is. If it's in the same folder where `WhatsThePass.jar` is, then you can just enter the name of the file (in the above example it's `1password-recovery`). If it's somewhere else, make sure to specify the complete path (e.g. `C:\My\Folder\RecoveryFiles\1password-recovery.wtp.json`).

If you've written an extra comment during recovery file creation in the past, then that comment will be displayed. Now all you need to do is to answer the questions **Whats The Pass* asks you, one by one. 

> **NOTE:** answers are not case-sensetive. That is, `red`, `Red`, `RED`, `rEd`, and so on are all treated the same.

If you answer everything correctly, your data will be decrypted and displayed. Congratulations!

![](https://blob.sx/ehln)


# Under the hood

This part is for those interested in details on how **What The Pass** stores their data. 

Recovery files' format is `JSON`. In the above example it looks like this:

```json
{
  "questions": [
    "What was the initial color of the first car bought? (before repainting)"
  ],
  "salts": [
    "q9ErFGibIZw="
  ],
  "encryptedData": "RO1HDDq9MERtnXlFH8QR1UC7yKlWNXoS+7QKv/iP+hPwUzcN+igoa3t0eguedfh7",
  "comment": "The car was not white when I bought it..."
}
```

As you can see, it stores four things:
1. the questions entered during recovery file creation **(without answers!)**;
2. Base64-encoded 8-bytes salts that were automatically generated by **Whats The Pass** during encryption. One question — one salt.
3. the encrypted data (in the above example, it is `123qweMyC00lPass`). The encryption process is: 
    * encode the plain text (`123qweMyC00lPass`) with Base64;
    * encrypt the encoded plain text with AES-256, using answer to question #1 as the key, with a freshly generated 8-bytes salt;
    * repeat the previous step, using answers to questions 2, 3, ..., n as the key, always generating a new 8-bytes salt (that is, we basically encrypt data that is already encrypted, multiple times);
    * encode the encrypted array of bytes with Base64 — the result string is `encryptedData`;
4. an extra comment string displayed to the user when running the process of data recovery — **may be empty**.

If one of the answers you enter during the process of recovery is incorrect, then **Whats The Pass** will fail to decrypt `encryptedData` **(due to an invalid key being used)**, and hence the data will not be displayed. That is, if you forget answers to your questions, the data will be lost permanently. At the same time this means that if someone else has gotten access to your recovery file, **if they know the correct answers to your questions, they can reveal `encryptedData`!** This is why the questions should be really secret/personal.


# License

[Apache License 2.0](https://github.com/MeGysssTaa/whats-the-pass/blob/master/LICENSE)
