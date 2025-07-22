package com.example.internhunt

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.MessagingException
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

class JakartaMailSender(
    private val senderEmail: String,
    private val senderPassword: String
) {
    fun sendEmail(toEmail: String, subject: String, body: String) {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(senderEmail, senderPassword)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                setSubject(subject)
                setText(body)
            }

            Transport.send(message)
        } catch (e: MessagingException) {
            e.printStackTrace()
            throw e
        }
    }
}
