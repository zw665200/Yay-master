package com.ql.recovery.yay.ui.login

import android.app.ActivityOptions
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityLoginBinding
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.ui.MainActivity
import com.ql.recovery.yay.ui.auth.AuthActivity
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.guide.GuideActivity
import com.ql.recovery.yay.ui.mine.AgreementActivity
import com.ql.recovery.yay.util.JLog
import com.ql.recovery.yay.util.ToastUtil
import com.tencent.mmkv.MMKV


class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInOptions: GoogleSignInOptions
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var facebookCallback: FacebookCallback<LoginResult>? = null
    private var facebookManager: CallbackManager? = null
    private var exoPlayer: ExoPlayer? = null
    private var uri = "https://picpro-cn.oss-cn-shenzhen.aliyuncs.com/feedback/login.mp4"

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityLoginBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.googleLogin.setOnClickListener { loginWithGoogle() }
        binding.userAgreement.setOnClickListener { toAgreementPage() }
        binding.privacyAgreement.setOnClickListener { toAgreementPage() }
        binding.facebookLogin.setOnClickListener { loginWithFacebook() }
        binding.phoneLogin.setOnClickListener { toPhoneLoginPage() }
        binding.whatsAppLogin.setOnClickListener { loginWithTest() }
    }

    override fun initData() {
        initGoogleLoginService()

        firebaseAnalytics = Firebase.analytics
        exoPlayer = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(uri)
        binding.playerView.player = exoPlayer
        exoPlayer!!.setMediaItem(mediaItem)
        exoPlayer!!.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer!!.prepare()
        exoPlayer!!.playWhenReady = true
    }

    private fun initGoogleLoginService() {
        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.login_google_web_client_id))
            .requestServerAuthCode(getString(R.string.login_google_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

//        oneTapClient = Identity.getSignInClient(this)
//        signInRequest = BeginSignInRequest.builder()
//            .setPasswordRequestOptions(
//                BeginSignInRequest.PasswordRequestOptions.builder()
//                    .setSupported(true)
//                    .build()
//            )
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    // Your server's client ID, not your Android client ID.
//                    .setServerClientId(getString(R.string.login_google_web_client_id))
//                    // Only show accounts previously used to sign in.
//                    .setFilterByAuthorizedAccounts(false)
//                    .build()
//            )
//            // Automatically sign in when exactly one credential is retrieved.
//            .setAutoSelectEnabled(true)
//            .build()
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                val signInCredentials = Identity.getSignInClient(this)
                    .getSignInCredentialFromIntent(result.data)
                // Review the Verify the integrity of the ID token section for
                // details on how to verify the ID token
//                verifyIdToken(signInCredentials.googleIdToken)
            } catch (e: ApiException) {

            }
        } else {

        }
    }

    private fun loginWithGoogle() {
        if (!binding.agreementCheck.isChecked) {
            ToastUtil.showShort(this, getString(R.string.login_agreement))
            return
        }

        //检查用户是否已经登录
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account == null) {

            //没有登录则发起登录
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 0x1001)

            //最新登录API
//            val request = GetSignInIntentRequest.builder().setServerClientId(getString(R.string.login_google_web_client_id)).build()
//            Identity.getSignInClient(this)
//                .getSignInIntent(request)
//                .addOnSuccessListener { result ->
//                    try {
//                        startIntentSenderForResult(
//                            result.intentSender,
//                            REQUEST_CODE_GOOGLE_SIGN_IN,
//                            /* fillInIntent= */ null,
//                            /* flagsMask= */ 0,
//                            /* flagsValue= */ 0,
//                            /* extraFlags= */ 0,
//                            /* options= */ null
//                        );
//                    } catch (e: IntentSender.SendIntentException) {
//                        JLog.i("Google Sign-in failed")
//                    }
//                }
//                .addOnFailureListener { e ->
//                    JLog.e("Google Sign-in failed : $e")
//                }

            //一键登录
//            oneTapClient.beginSignIn(signInRequest)
//                .addOnSuccessListener(this) {
//                    try {
//                        startIntentSenderForResult(
//                            it.pendingIntent.intentSender, 0x1001,
//                            null, 0, 0, 0
//                        )
//                    } catch (e: IntentSender.SendIntentException) {
//                        JLog.i("Couldn't start One Tap UI: " + e.localizedMessage)
//                    }
//                }.addOnFailureListener {
//                    ToastUtil.showShort(this, it.localizedMessage)
//                    JLog.i(it.localizedMessage)
//                }

        } else {
            val email = account.email
            val token = account.idToken
            val icon = account.photoUrl

            JLog.i("email = $email")
            JLog.i("token = $token")

            if (token != null) {
                logoutWithGoogle()
            } else {
                loginWithGoogle()
            }
        }
    }


    private fun loginWithFacebook() {
        if (!binding.agreementCheck.isChecked) {
            ToastUtil.showShort(this, getString(R.string.login_agreement))
            return
        }

        //检查登录状态
        val currentToken = AccessToken.getCurrentAccessToken()
        if (currentToken != null && !currentToken.isExpired) {
            getAuthFromFacebook(currentToken.token)
            return
        }

        facebookManager = CallbackManager.Factory.create()
        facebookCallback = object : FacebookCallback<LoginResult> {
            override fun onCancel() {
                JLog.i("onCancel")
            }

            override fun onError(error: FacebookException) {
                JLog.i("error = ${error.message}")
            }

            override fun onSuccess(result: LoginResult) {
                JLog.i("result = $result")
                JLog.i("token = ${result.accessToken.token}")
                val token = result.accessToken.token
                getAuthFromFacebook(token)
            }
        }

        LoginManager.getInstance().registerCallback(facebookManager, facebookCallback)
        LoginManager.getInstance().loginBehavior = LoginBehavior.NATIVE_WITH_FALLBACK
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
    }

    private fun loginWithTest() {
        if (!binding.agreementCheck.isChecked) {
            ToastUtil.showShort(this, getString(R.string.login_agreement))
            return
        }

        loginWithPhone("12345678", "15011352575", "+86") { loadUserInfo() }
    }


    private fun toAgreementPage() {
        val intent = Intent(this, AgreementActivity::class.java)
        startActivity(intent)
    }

    private fun toPhoneLoginPage() {
        startActivity(
            Intent(this, PhoneLoginActivity::class.java),
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )
    }

    private fun logoutWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            JLog.i("logout success")
            loginWithGoogle()
        }
    }

    @Deprecated("")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookManager?.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            0x1001 -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        val email = account.email
                        val token = account.idToken
                        val icon = account.photoUrl

                        JLog.i("get email = $email")
                        JLog.i("get token = $token")
                        JLog.i("get icon = $icon")

                        if (token != null) {
                            getAuthFromGoogle(token)
                        }
                    }

                    //一键登录查询结果
//                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
//                    val idToken = credential.googleIdToken
//                    val username = credential.id
//                    val password = credential.password
//                    when {
//                        idToken != null -> {
//                            // Got an ID token from Google. Use it to authenticate
//                            // with your backend.
//                            JLog.i("Got username.")
//                            JLog.i("Got ID token.")
//                            JLog.i("username = $username")
//                            JLog.i("token = $idToken")
//                            getAuthFromGoogle(idToken)
//                        }
//                        password != null -> {
//                            // Got a saved username and password. Use them to authenticate
//                            // with your backend.
//                            JLog.i("Got password.")
//                        }
//                        else -> {
//                            // Shouldn't happen.
//                            JLog.i("No ID token or password!")
//                        }
//                    }

                } catch (e: ApiException) {
                    val content = "signInResult:failed code = ${e.statusCode} , message = ${e.status}"
                    JLog.i(content)
                    ToastUtil.showShort(this, content)
                }
            }
        }
    }

    private fun getAuthFromGoogle(token: String) {
        DataManager.getAuthFromGoogle(this, token) {
            val accessToken = it.type + " " + it.access_token
            Config.CLIENT_TOKEN = accessToken
            MMKV.defaultMMKV()?.encode("access_token", accessToken)
            MMKV.defaultMMKV()?.encode("token", it.access_token)
            loadUserInfo()
        }
    }

    private fun getAuthFromFacebook(token: String) {
        DataManager.getAuthFromFacebook(this, token) {
            val accessToken = it.type + " " + it.access_token
            Config.CLIENT_TOKEN = accessToken
            MMKV.defaultMMKV()?.encode("access_token", accessToken)
            MMKV.defaultMMKV()?.encode("token", it.access_token)
            loadUserInfo()
        }
    }

    fun loginWithPhone(password: String, phone: String, phoneCode: String, isSuccess: (Boolean) -> Unit) {
        DataManager.getAuthFromPhone(password, phone, phoneCode) {
            val accessToken = it.type + " " + it.access_token
            Config.CLIENT_TOKEN = accessToken
            MMKV.defaultMMKV()?.encode("access_token", accessToken)
            MMKV.defaultMMKV()?.encode("token", it.access_token)
            isSuccess(true)
        }
    }

    private fun loadUserInfo() {
        DataManager.getUserInfo { userInfo ->
            ToastUtil.showShort(this, getString(R.string.login_success))

            //刷新用户信息
            Config.mainHandler?.sendEmptyMessage(0x10006)

            //登录IM
            Config.mHandler?.sendEmptyMessage(0x10004)

            //上报日志
            ReportManager.firebaseLoginLog(firebaseAnalytics, userInfo.uid, userInfo.nickname)
            ReportManager.facebookLoginLog(this, userInfo.uid, userInfo.nickname)
            ReportManager.branchLoginLog(this, userInfo.uid, userInfo.nickname)

            val guide = getLocalStorage().decodeBool("guide_finish", false)
            if (!guide) {
                if (userInfo.sex == 0 || userInfo.age == 0 || userInfo.avatar.isBlank() || userInfo.nickname.isBlank()
                    || userInfo.photos.isEmpty() || userInfo.tags.isEmpty()
                ) {
                    startActivity(Intent(this, GuideActivity::class.java))
                    finish()
                    return@getUserInfo
                }
            }

            val permission = getLocalStorage().decodeBool("show_permission", false)
            if (!permission) {
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onBackPressed() {
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }

    override fun onStop() {
        super.onStop()
//        exoPlayer?.stop()
    }

}