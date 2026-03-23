package fr.mma.df.codinggame.api.feature.mail

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailService(private val javaMailSender: JavaMailSender) {

    fun sendSignupCodeEmail(mail: String, token: String) {
        val message = SimpleMailMessage()
        message.setTo(mail)
        message.setSubject("Votre code d'inscription")
        message.setText("Voici votre token : $token\n\nConsultez la doc ici : https://df24mma.atlassian.net/browse/DF-27")
        javaMailSender.send(message)
    }

    fun sendPlayerTokenEmail(mail: String, token: String) {
        val message = SimpleMailMessage()
        message.setTo(mail)
        message.setSubject("Votre code d'inscription")
        message.setText("Voici votre token : $token\n\nConsultez la doc ici : https://df24mma.atlassian.net/browse/DF-27")
        javaMailSender.send(message)
    }
}
