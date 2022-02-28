package quickcarpet.build

import java.io.File
import java.nio.file.*

import java.awt.GridLayout
import java.util.*
import javax.swing.*

import com.atlauncher.*
import com.atlauncher.data.*
import com.atlauncher.gui.dialogs.LoginWithMicrosoftDialog
import com.atlauncher.managers.AccountManager
import com.atlauncher.utils.Authentication

object AuthHelper {
    fun setupAuth(dir: File): Map<String, String>? {
        val atlDir = dir.toPath()
        App.workingDir = atlDir
        Files.createDirectories(atlDir.resolve("configs"))
        App.launcher = object : Launcher() {
            override fun reloadFeaturedPacksPanel() {}
            override fun refreshFeaturedPacksPanel() {}
            override fun reloadPacksBrowserPanel() {}
            override fun refreshPacksBrowserPanel() {}
        }
        val loadSettings = App::class.java.getDeclaredMethod("loadSettings")
        loadSettings.setAccessible(true)
        loadSettings.invoke(null)
        AccountManager.loadAccounts()
        var selectedAccount = AccountManager.getSelectedAccount() ?: if (AccountManager.getAccounts().isEmpty()) null else AccountManager.getAccounts().get(0)
        if (selectedAccount == null) {
            val chosen = JOptionPane.showOptionDialog(null, "Select account type", "Setup Minecraft Account", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, arrayOf("Microsoft", "Mojang"), "Microsoft")
            selectedAccount = when (chosen) {
                0 -> {
                    val dialog = LoginWithMicrosoftDialog()
                    while (dialog.isVisible()) {
                        Thread.sleep(100)
                    }
                    if (AccountManager.getAccounts().isEmpty()) return null
                    AccountManager.getAccounts()[0]
                }
                1 -> {
                    val dialog = JPanel()
                    val usernameField = JTextField()
                    val passwordField = JPasswordField()
                    dialog.layout = GridLayout(2, 2)
                    dialog.add(JLabel("Username/E-Mail"))
                    dialog.add(usernameField)
                    dialog.add(JLabel("Password"))
                    dialog.add(passwordField)
                    JOptionPane.showMessageDialog(null, dialog, "Enter Mojang account credentials", JOptionPane.QUESTION_MESSAGE)
                    val clientToken = UUID.randomUUID().toString().replace("-", "")
                    val username = usernameField.text
                    val password = String(passwordField.password)
                    val resp = Authentication.checkAccount(username, password, clientToken)
                    if (resp == null || !resp.hasAuth() || !resp.isValidAuth()) return null
                    MojangAccount(username, password, resp, true, clientToken).also { AccountManager.addAccount(it) }
                }
                else -> {
                    return null
                }
            }
        }

        if (selectedAccount is MojangAccount) {
            selectedAccount.login()
        } else {
            (selectedAccount as MicrosoftAccount).ensureAccessTokenValid()
        }

        return mapOf(
            "accessToken" to selectedAccount.getAccessToken(),
            "uuid" to selectedAccount.getRealUUID().toString().replace("-", ""),
            "username" to selectedAccount.minecraftUsername
        )
    }
}