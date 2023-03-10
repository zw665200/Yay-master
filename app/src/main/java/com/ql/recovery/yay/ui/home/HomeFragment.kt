package com.ql.recovery.yay.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.config.ChooseType
import com.ql.recovery.yay.databinding.FragmentHomeBinding
import com.ql.recovery.yay.manager.ImageManager
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.ui.MainActivity
import com.ql.recovery.yay.ui.base.BaseFragment
import com.ql.recovery.yay.ui.dialog.CompleteProfileDialog
import com.ql.recovery.yay.ui.dialog.FilterDialog
import com.ql.recovery.yay.ui.dialog.PrimeDialog
import com.ql.recovery.yay.ui.match.MatchActivity
import com.ql.recovery.yay.ui.store.StoreActivity
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.DoubleUtils
import com.tencent.mmkv.MMKV

class HomeFragment : BaseFragment() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var binding: FragmentHomeBinding? = null
    private var type = Type.VIDEO
    private var mk = MMKV.defaultMMKV()
    private var lastClick = 0L

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        initClubLottie()
        loadVideoLottie()
        loadAudioLottie()
        loadGameLottie()

        firebaseAnalytics = Firebase.analytics

        binding?.ivVip?.setOnClickListener { showPrimeDialog() }
        binding?.tvVideoMatch?.setOnClickListener { checkVideoMatch() }
        binding?.tvAudioMatch?.setOnClickListener { checkAudioMatch() }
        binding?.tvImMatch?.setOnClickListener { checkGameMatch() }
        binding?.flMatchBegin?.setOnClickListener { toMatchPage() }
        binding?.llChooseGender?.setOnClickListener { showFilterDialog(ChooseType.Gender) }
        binding?.llChooseRegion?.setOnClickListener { showFilterDialog(ChooseType.Region) }
        binding?.llCoin?.setOnClickListener { toStorePage() }
        binding?.ivVip?.setOnClickListener { showPrimeDialog() }
        binding?.lottieClub?.setOnClickListener { (requireActivity() as MainActivity).changeFragment(2) }

        return binding!!.root
    }

    override fun initData() {
        getUserInfo()
        flushConfig()
        loadOnlineCount()
    }

    private fun initClubLottie() {
        val width = AppUtil.getScreenWidth(requireContext())
        val w = width / 5
        val lp = binding!!.lottieClub.layoutParams
        lp.width = w
        lp.height = 92 * w / 60
        binding!!.lottieClub.layoutParams = lp

        binding!!.lottieClub.imageAssetsFolder = "loading/club"
        binding!!.lottieClub.setAnimation("club_data.json")
        binding!!.lottieClub.playAnimation()
    }

    private fun loadVideoLottie() {
        val width = AppUtil.getScreenWidth(requireContext())
        val params = binding!!.lottieVideo.layoutParams
        params.width = width
        params.height = 2436 * width / 1125
        binding!!.lottieVideo.layoutParams = params

        binding!!.lottieVideo.imageAssetsFolder = "loading/match/video"
        binding!!.lottieVideo.setAnimation("loading/match/match_video_data.json")
        binding!!.lottieVideo.playAnimation()
    }

    private fun loadAudioLottie() {
        val width = AppUtil.getScreenWidth(requireContext())
        val params = binding!!.lottieVoice.layoutParams
        params.width = width
        params.height = 2436 * width / 1125
        binding!!.lottieVoice.layoutParams = params

        binding!!.lottieVoice.imageAssetsFolder = "loading/match/audio"
        binding!!.lottieVoice.setAnimation("loading/match/match_audio_data.json")
        binding!!.lottieVoice.playAnimation()
    }

    private fun loadGameLottie() {
        val width = AppUtil.getScreenWidth(requireContext())
        val params = binding!!.lottieGame.layoutParams
        params.width = width
        params.height = 2436 * width / 1125
        binding!!.lottieGame.layoutParams = params

        binding!!.lottieGame.imageAssetsFolder = "loading/match/game"
        binding!!.lottieGame.setAnimation("loading/match/match_game_data.json")
        binding!!.lottieGame.playAnimation()
    }

    private fun getUserInfo() {
        getUserInfo {
            binding?.tvCoin?.text = it.coin.toString()
            if (it.is_vip) {
                binding?.ivVip?.setImageResource(R.drawable.in_vip)
            } else {
                binding?.ivVip?.setImageResource(R.drawable.vip_c)
            }
        }
    }

    override fun setOnlineStatus(uid: String, online: Boolean) {
    }

    override fun refreshUserInfo() {
        getUserInfo()
        flushConfig()
    }

    override fun refreshOnlineTime() {}

    private fun flushConfig() {
        val config = getMatchConfig()
        when (config.target_sex) {
            0 -> {
                binding?.tvSex?.text = getString(R.string.home_all_gender)
                binding?.ivSex?.setImageResource(R.drawable.in_xb)
            }
            1 -> {
                binding?.tvSex?.text = getString(R.string.home_male)
                binding?.ivSex?.setImageResource(R.drawable.man)
            }
            2 -> {
                binding?.tvSex?.text = getString(R.string.home_female)
                binding?.ivSex?.setImageResource(R.drawable.woman)
            }
        }

        if (config.country_name.isBlank()) {
            binding?.tvRegion?.text = getString(R.string.home_match_global)
        } else {
            binding?.tvRegion?.text = config.country_name
        }

        if (config.country_locale.isNotBlank()) {
            val res = "file:///android_asset/images/${config.country_locale}.png"
            ImageManager.getBitmap(requireContext(), res) { bitmap ->
                binding?.ivRegion?.setImageBitmap(bitmap)
            }
        } else {
            val res = "file:///android_asset/images/GLOBAL.png"
            ImageManager.getBitmap(requireContext(), res) { bitmap ->
                binding?.ivRegion?.setImageBitmap(bitmap)
            }
        }
    }

    private fun loadOnlineCount() {
        DataManager.getOnlineCount { count ->
            binding?.tvOnlineCount?.text = count.toString()
        }
    }

    private fun toStorePage() {
        if (!DoubleUtils.isFastDoubleClick()) {
            startActivity(Intent(requireActivity(), StoreActivity::class.java))
        }
    }

    override fun click(v: View) {
    }

    private fun checkVideoMatch() {
        type = Type.VIDEO
        binding?.tvVideoMatch?.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow_4, null)
        binding?.tvAudioMatch?.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rectangle_white_4, null)
        binding?.tvImMatch?.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rectangle_white_4, null)
        binding?.tvVideoMatch?.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
        binding?.tvAudioMatch?.setTextColor(ResourcesCompat.getColor(resources, R.color.color_white, null))
        binding?.tvImMatch?.setTextColor(ResourcesCompat.getColor(resources, R.color.color_white, null))
        binding?.lottieVideo?.visibility = View.VISIBLE
        binding?.lottieVoice?.visibility = View.GONE
        binding?.lottieGame?.visibility = View.GONE
    }

    private fun checkAudioMatch() {
        type = Type.VOICE
        binding?.tvVideoMatch?.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rectangle_white_4, null)
        binding?.tvAudioMatch?.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow_4, null)
        binding?.tvImMatch?.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rectangle_white_4, null)
        binding?.tvVideoMatch?.setTextColor(ResourcesCompat.getColor(resources, R.color.color_white, null))
        binding?.tvAudioMatch?.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
        binding?.tvImMatch?.setTextColor(ResourcesCompat.getColor(resources, R.color.color_white, null))
        binding?.lottieVideo?.visibility = View.GONE
        binding?.lottieVoice?.visibility = View.VISIBLE
        binding?.lottieGame?.visibility = View.GONE
    }

    private fun checkGameMatch() {
        type = Type.GAME
        binding?.tvVideoMatch?.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rectangle_white_4, null)
        binding?.tvAudioMatch?.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rectangle_white_4, null)
        binding?.tvImMatch?.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow_4, null)
        binding?.tvVideoMatch?.setTextColor(ResourcesCompat.getColor(resources, R.color.color_white, null))
        binding?.tvAudioMatch?.setTextColor(ResourcesCompat.getColor(resources, R.color.color_white, null))
        binding?.tvImMatch?.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
        binding?.lottieVideo?.visibility = View.GONE
        binding?.lottieVoice?.visibility = View.GONE
        binding?.lottieGame?.visibility = View.VISIBLE
    }

    private fun toMatchPage() {
        if (lastClick == 0L) {
            lastClick = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - lastClick < 2000L) {
            return
        }

        getUserInfo { userInfo ->
            //report
            initReport(userInfo, "match_first_click")

            checkVideoPermissions {
                //???????????????????????????
                val firstMatch = getLocalStorage().decodeBool("first_match", false)
                if (!firstMatch) {
                    openMatchPage()
                    return@checkVideoPermissions
                }

                //????????????????????????????????????????????????????????????????????????
                if (userInfo.sex == 0 || userInfo.age == 0 || userInfo.avatar.isBlank() ||
                    userInfo.nickname.isBlank() || userInfo.photos.isEmpty()
                ) {
                    //????????????????????????
                    CompleteProfileDialog(requireActivity(), userInfo) {
                        openMatchPage()
                    }
                } else {
                    openMatchPage()
                }
            }
        }
    }

    private fun openMatchPage() {
        when (type) {
            Type.VIDEO -> {
                val intent = Intent(requireActivity(), MatchActivity::class.java)
                intent.putExtra("type", "video")
                startActivity(intent)

                ReportManager.firebaseCustomLog(firebaseAnalytics, "match_video_begin", "begin match video")
                ReportManager.appsFlyerCustomLog(requireContext(), "match_video_begin", "begin match video")
            }

            Type.VOICE -> {
                val intent = Intent(requireActivity(), MatchActivity::class.java)
                intent.putExtra("type", "voice")
                startActivity(intent)

                ReportManager.firebaseCustomLog(firebaseAnalytics, "match_voice_begin", "begin match voice")
                ReportManager.appsFlyerCustomLog(requireContext(), "match_voice_begin", "begin match voice")
            }

            Type.GAME -> {
                val intent = Intent(requireActivity(), MatchActivity::class.java)
                intent.putExtra("type", "game")
                startActivity(intent)

                ReportManager.firebaseCustomLog(firebaseAnalytics, "match_game_begin", "begin match game")
                ReportManager.appsFlyerCustomLog(requireContext(), "match_game_begin", "begin match game")
            }
        }
    }

    /**
     * ???????????????????????????????????????
     */
    private fun initReport(userInfo: UserInfo, customName: String) {
        val report = getLocalStorage().decodeBool(customName, false)
        if (!report) {
            getLocalStorage().encode(customName, true)
            ReportManager.firebaseCustomLog(firebaseAnalytics, customName, userInfo.nickname)
            ReportManager.facebookCustomLog(requireContext(), customName, userInfo.nickname)
            ReportManager.branchCustomLog(requireContext(), customName, null)
            ReportManager.appsFlyerCustomLog(requireContext(), customName, userInfo.nickname)
        }
    }

    private fun showPrimeDialog() {
        val userInfo = mk.decodeParcelable("user_info", UserInfo::class.java) ?: return
        PrimeDialog(requireActivity(), userInfo.is_vip) {}
    }

    private fun showFilterDialog(type: ChooseType) {
        if (!DoubleUtils.isFastDoubleClick()) {
            getUserInfo { userInfo ->
                getBasePrice {
                    when (type) {
                        ChooseType.Gender -> {
                            ReportManager.firebaseCustomLog(firebaseAnalytics, "gender_filter_click", "gender filter")
                            ReportManager.appsFlyerCustomLog(requireContext(), "gender_filter_click", "gender filter")
                            FilterDialog(requireActivity(), userInfo, it, getMatchConfig(), ChooseType.Gender) {
                                flushConfig()
                            }
                        }

                        ChooseType.Region -> {
                            ReportManager.firebaseCustomLog(firebaseAnalytics, "region_filter_click", "region filter")
                            ReportManager.appsFlyerCustomLog(requireContext(), "region_filter_click", "region filter")
                            FilterDialog(requireActivity(), userInfo, it, getMatchConfig(), ChooseType.Region) {
                                flushConfig()
                            }
                        }
                    }
                }
            }
        }
    }

    enum class Type { VIDEO, VOICE, GAME }
}