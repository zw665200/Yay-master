package com.ql.recovery.yay.ui.guide

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.uinfo.UserService
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.Tag
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.FileCallback
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityGuideBinding
import com.ql.recovery.yay.databinding.ItemGuideTagBinding
import com.ql.recovery.yay.databinding.ItemWallpaperBinding
import com.ql.recovery.yay.manager.CManager
import com.ql.recovery.yay.manager.ImageManager
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.ui.MainActivity
import com.ql.recovery.yay.ui.auth.AuthActivity
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.WaitingDialog
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.FileUtil
import com.ql.recovery.yay.util.ToastUtil
import com.weigan.loopview.LoopView
import com.weigan.loopview.OnItemScrollListener

class GuideActivity : BaseActivity() {
    private lateinit var binding: ActivityGuideBinding
    private var amType: GenderType? = null
    private var findType: GenderType? = null
    private var mIsMale: Int? = null
    private var mFindIsMale: Int? = null
    private var mBirthday: String? = null
    private var mNickname: String? = null
    private var mAvatar: String? = null
    private var mAlbumList = arrayListOf<String>()
    private var mTargetList = arrayListOf<Int>()
    private var currentYear: String? = null
    private var currentMonth: String? = null
    private var currentDay: String? = null
    private var currentUserInfo: UserInfo? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var mAdapter: DataAdapter<Bitmap>
    private lateinit var mTagAdapter: DataAdapter<Tag>
    private var mList = arrayListOf<Bitmap>()
    private var mTagList = arrayListOf<Tag>()

    private var dialog: WaitingDialog? = null
    private var jumpList = arrayListOf<String>()
    private var stepList = arrayListOf<Step>()
    private var step = Step.Gender

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityGuideBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.includeHead.ivBack.setOnClickListener { checkParam() }
        binding.includeGender.ivAmMale.setOnClickListener { chooseGender(ChooseType.I, GenderType.Male) }
        binding.includeGender.ivAmFemale.setOnClickListener { chooseGender(ChooseType.I, GenderType.Female) }
        binding.includeGender.ivFindMale.setOnClickListener { chooseGender(ChooseType.Find, GenderType.Male) }
        binding.includeGender.ivFindFemale.setOnClickListener { chooseGender(ChooseType.Find, GenderType.Female) }
        binding.includeAvatar.ivAvatar.setOnClickListener { openAlbum(0x1002) }
        binding.tvNextStep.setOnClickListener { doTask() }
        binding.tvJump.setOnClickListener { checkJump() }
    }

    override fun initData() {
        dialog = WaitingDialog(this)
        firebaseAnalytics = Firebase.analytics
        checkStep()
    }

    private fun checkStep() {
        DataManager.getUserInfo {
            currentUserInfo = it
            getLocalStorage().encode("user_info", it)

            if (it.sex == 0) {
                step = Step.Gender
                binding.includeGender.root.visibility = View.VISIBLE
                binding.includeHead.tvName.text = getString(R.string.guide_title_gender)
                return@getUserInfo
            } else {
                binding.includeGender.root.visibility = View.GONE
            }

            if (it.age == 0) {
                step = Step.Birthday
                binding.includeBirthday.root.visibility = View.VISIBLE
                binding.includeHead.tvName.text = getString(R.string.guide_title_age)
                chooseBirthDay()
                return@getUserInfo
            } else {
                binding.includeBirthday.root.visibility = View.GONE
            }

            if (it.nickname.isBlank()) {
                step = Step.NickName
                binding.includeNickname.root.visibility = View.VISIBLE
                binding.includeHead.tvName.text = getString(R.string.guide_title_nickname)
                return@getUserInfo
            } else {
                binding.includeNickname.root.visibility = View.GONE
            }

            if (it.avatar.isBlank() && !jumpList.contains("avatar")) {
                step = Step.Avatar
                binding.includeAvatar.root.visibility = View.VISIBLE
                binding.tvJump.visibility = View.VISIBLE
                binding.includeHead.tvName.text = getString(R.string.guide_title_avatar)
                return@getUserInfo
            } else {
                binding.includeAvatar.root.visibility = View.GONE
            }

            if (it.photos.isEmpty() && !jumpList.contains("albums")) {
                step = Step.Album
                binding.includeWallpaper.root.visibility = View.VISIBLE
                binding.tvJump.visibility = View.VISIBLE
                binding.includeHead.tvName.text = getString(R.string.guide_title_wallpaper)
                chooseWallpaper()
                return@getUserInfo
            } else {
                binding.includeWallpaper.root.visibility = View.GONE
            }

            if (it.tags.isEmpty() && !jumpList.contains("tags")) {
                step = Step.Tags
                binding.includeTags.root.visibility = View.VISIBLE
                binding.tvJump.visibility = View.VISIBLE
                binding.includeHead.tvName.text = getString(R.string.guide_title_fun)
                chooseTags()
                return@getUserInfo
            }

            getLocalStorage().encode("guide_finish", true)

            ReportManager.firebaseCustomLog(firebaseAnalytics, "guide_leave", "leave guide page")
            ReportManager.appsFlyerCustomLog(this, "guide_leave", "leave guide page")

            val permission = getLocalStorage().decodeBool("show_permission", false)
            if (!permission) {
                toAuthPage()
            } else {
                toMainPage()
            }
        }
    }

    private fun checkJump() {
        if (currentUserInfo == null) return
        if (currentUserInfo!!.avatar.isBlank()) {
            jumpList.add("avatar")
            checkStep()
            return
        }

        if (currentUserInfo!!.photos.isEmpty()) {
            jumpList.add("albums")
            checkStep()
            return
        }

        if (currentUserInfo!!.tags.isEmpty()) {
            jumpList.add("tags")
            checkStep()
        }
    }

    private fun doTask() {
        if (currentUserInfo == null) return
        when (step) {
            Step.Gender -> {
                if (amType != null) {
                    mIsMale = when (amType!!) {
                        GenderType.Male -> 1
                        GenderType.Female -> 2
                    }
                }

                if (findType != null) {
                    mFindIsMale = when (findType!!) {
                        GenderType.Male -> 1
                        GenderType.Female -> 2
                    }
                }

                updateUserInfo(null, null, null, mIsMale, mFindIsMale, null, null)
                ReportManager.firebaseCustomLog(firebaseAnalytics, "modify_gender", "modify gender")
            }

            Step.Birthday -> {
                if (currentYear != null && currentMonth != null && currentDay != null) {
                    if (currentMonth!!.length == 1) {
                        currentMonth = "0$currentMonth"
                    }
                    if (currentDay!!.length == 1) {
                        currentDay = "0$currentDay"
                    }

                    mBirthday = "$currentYear-$currentMonth-$currentDay 00:00:00"

                    val year = AppUtil.getTodayYear()
                    if (year.toInt() - currentYear!!.toInt() < 18) {
                        ToastUtil.showShort(this, getString(R.string.guide_notice_limit_age))
                        return
                    }
                }

                updateUserInfo(null, mBirthday, null, null, null, null, null)
                ReportManager.firebaseCustomLog(firebaseAnalytics, "modify_birthday", "modify birthday")
            }

            Step.NickName -> {
                if (mNickname.isNullOrBlank()) {
                    val nickname = binding.includeNickname.etPhoneInput.editableText.toString()
                    if (nickname.isBlank()) {
                        ToastUtil.showShort(this, getString(R.string.guide_nickname_hint))
                        return
                    }
                    mNickname = nickname
                    updateUserInfo(null, null, nickname, null, null, null, null)
                    ReportManager.firebaseCustomLog(firebaseAnalytics, "modify_nickname", "modify nickname")

                    //更新用户资料
                    val fields = HashMap<UserInfoFieldEnum, Any>()
                    fields[UserInfoFieldEnum.Name] = nickname
                    NIMClient.getService(UserService::class.java).updateUserInfo(fields)
                }
            }

            Step.Avatar -> {
                if (mAvatar.isNullOrBlank() && !jumpList.contains("avatar")) {
                    ToastUtil.showShort(this, getString(R.string.guide_avatar_setting))
                    return
                }
                updateUserInfo(mAvatar, null, null, null, null, null, null)
                ReportManager.firebaseCustomLog(firebaseAnalytics, "modify_avatar", "modify avatar")
            }

            Step.Album -> {
                if (mAlbumList.isEmpty() && !jumpList.contains("albums")) {
                    ToastUtil.showShort(this, getString(R.string.guide_album_tip))
                    return
                }
                updateUserInfo(null, null, null, null, null, mAlbumList, null)
                ReportManager.firebaseCustomLog(firebaseAnalytics, "modify_pic", "modify pics")
            }

            Step.Tags -> {
                if (mTargetList.isEmpty() && !jumpList.contains("tags")) {
                    ToastUtil.showShort(this, getString(R.string.guide_tag_tip))
                    return
                }
                updateUserInfo(null, null, null, null, null, null, mTargetList)
                ReportManager.firebaseCustomLog(firebaseAnalytics, "modify_short_video", "modify short videos")
            }
        }
    }

    private fun chooseGender(chooseType: ChooseType, genderType: GenderType) {
        when (chooseType) {
            ChooseType.I -> {
                when (genderType) {
                    GenderType.Male -> {
                        when (amType) {
                            GenderType.Male -> {
                                amType = null
                                binding.includeGender.ivAmMale.setImageResource(R.drawable.male)
                                binding.includeGender.ivAmFemale.setImageResource(R.drawable.female)
                            }

                            GenderType.Female -> {
                                amType = GenderType.Male
                                binding.includeGender.ivAmMale.setImageResource(R.drawable.male_find)
                                binding.includeGender.ivAmFemale.setImageResource(R.drawable.female)
                            }

                            null -> {
                                amType = GenderType.Male
                                binding.includeGender.ivAmMale.setImageResource(R.drawable.male_find)
                            }
                        }
                    }

                    GenderType.Female -> {
                        when (amType) {
                            GenderType.Male -> {
                                amType = GenderType.Female
                                binding.includeGender.ivAmMale.setImageResource(R.drawable.male)
                                binding.includeGender.ivAmFemale.setImageResource(R.drawable.female_find)
                            }

                            GenderType.Female -> {
                                amType = null
                                binding.includeGender.ivAmMale.setImageResource(R.drawable.male)
                                binding.includeGender.ivAmFemale.setImageResource(R.drawable.female)
                            }

                            null -> {
                                amType = GenderType.Female
                                binding.includeGender.ivAmFemale.setImageResource(R.drawable.female_find)
                            }
                        }
                    }
                }
            }

            ChooseType.Find -> {
                when (genderType) {
                    GenderType.Male -> {
                        when (findType) {
                            GenderType.Male -> {
                                findType = null
                                binding.includeGender.ivFindMale.setImageResource(R.drawable.male)
                                binding.includeGender.ivFindFemale.setImageResource(R.drawable.female)
                            }

                            GenderType.Female -> {
                                findType = GenderType.Male
                                binding.includeGender.ivFindMale.setImageResource(R.drawable.male_find)
                                binding.includeGender.ivFindFemale.setImageResource(R.drawable.female)
                            }

                            null -> {
                                findType = GenderType.Male
                                binding.includeGender.ivFindMale.setImageResource(R.drawable.male_find)
                            }
                        }
                    }

                    GenderType.Female -> {
                        when (findType) {
                            GenderType.Male -> {
                                findType = GenderType.Female
                                binding.includeGender.ivFindMale.setImageResource(R.drawable.male)
                                binding.includeGender.ivFindFemale.setImageResource(R.drawable.female_find)
                            }

                            GenderType.Female -> {
                                findType = null
                                binding.includeGender.ivFindMale.setImageResource(R.drawable.male)
                                binding.includeGender.ivFindFemale.setImageResource(R.drawable.female)
                            }

                            null -> {
                                findType = GenderType.Female
                                binding.includeGender.ivFindFemale.setImageResource(R.drawable.female_find)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun chooseBirthDay() {
        val yearList = AppUtil.getYearList()
        val monthList = AppUtil.getMonthList()
        val todayOfMonth = AppUtil.getTodayMonth()
        val dayList = arrayListOf<String>()

        binding.includeBirthday.loopYear.setItems(yearList)
        binding.includeBirthday.loopMonth.setItems(monthList)
        currentYear = yearList[18]
        currentMonth = todayOfMonth
        binding.includeBirthday.loopYear.setCurrentPosition(18)
        binding.includeBirthday.loopMonth.setCurrentPosition(12 - todayOfMonth.toInt())

        binding.includeBirthday.loopYear.setOnItemScrollListener(object : OnItemScrollListener {
            override fun onItemScrollStateChanged(
                loopView: LoopView?,
                currentPassItem: Int,
                oldScrollState: Int,
                scrollState: Int,
                totalScrollY: Int
            ) {
                currentYear = yearList[currentPassItem]
                val d = AppUtil.getMonthOfDay(currentYear!!.toInt(), currentMonth!!.toInt())
                dayList.clear()
                for (index in 0 until d) {
                    dayList.add((index + 1).toString())
                }
                binding.includeBirthday.loopDay.setItems(dayList)
            }

            override fun onItemScrolling(loopView: LoopView?, currentPassItem: Int, scrollState: Int, totalScrollY: Int) {
            }
        })

        binding.includeBirthday.loopMonth.setOnItemScrollListener(object : OnItemScrollListener {
            override fun onItemScrollStateChanged(
                loopView: LoopView?,
                currentPassItem: Int,
                oldScrollState: Int,
                scrollState: Int,
                totalScrollY: Int
            ) {
                currentMonth = monthList[currentPassItem]
                val d = AppUtil.getMonthOfDay(currentYear!!.toInt(), currentMonth!!.toInt())
                dayList.clear()
                for (index in 0 until d) {
                    dayList.add((index + 1).toString())
                }
                binding.includeBirthday.loopDay.setItems(dayList)
            }

            override fun onItemScrolling(loopView: LoopView?, currentPassItem: Int, scrollState: Int, totalScrollY: Int) {
            }
        })

        binding.includeBirthday.loopDay.setOnItemScrollListener(object : OnItemScrollListener {
            override fun onItemScrollStateChanged(
                loopView: LoopView?,
                currentPassItem: Int,
                oldScrollState: Int,
                scrollState: Int,
                totalScrollY: Int
            ) {
                currentDay = dayList[currentPassItem]
            }

            override fun onItemScrolling(loopView: LoopView?, currentPassItem: Int, scrollState: Int, totalScrollY: Int) {
            }
        })
    }

    private fun chooseWallpaper() {
        val width = AppUtil.getScreenWidth(this)
        mAdapter = DataAdapter.Builder<Bitmap>()
            .setData(mList)
            .setLayoutId(R.layout.item_wallpaper)
            .addBindView { itemView, itemData, position ->
                val itemWallpaperBinding = ItemWallpaperBinding.bind(itemView)
                val layout = itemView.layoutParams
                layout.width = width / 4
                layout.height = width / 4
                itemView.layoutParams = layout

                if (position == 9) {
                    itemView.visibility = View.GONE
                }

                Glide.with(this).load(itemData).into(itemWallpaperBinding.ivPic)

                itemView.setOnClickListener {
                    if (position == mList.size - 1) {
                        openAlbum(0x1001)
                    }
                }
            }
            .create()

        binding.includeWallpaper.rcImage.layoutManager = GridLayoutManager(this, 3)
        binding.includeWallpaper.rcImage.adapter = mAdapter

        mList.clear()
        mList.add(BitmapFactory.decodeResource(resources, R.drawable.my_tj))
        mAdapter.notifyItemInserted(0)
    }

    private fun chooseTags() {
        val width = AppUtil.getScreenWidth(this)
        mTagAdapter = DataAdapter.Builder<Tag>()
            .setData(mTagList)
            .setLayoutId(R.layout.item_guide_tag)
            .addBindView { itemView, itemData, position ->
                val itemGuideTagBinding = ItemGuideTagBinding.bind(itemView)
                val layout = itemView.layoutParams
                layout.width = width / 3
//                layout.height = width / 4
//                itemView.layoutParams = layout

                itemGuideTagBinding.tvName.text = itemData.name
                if (mTargetList.contains(itemData.id)) {
                    itemGuideTagBinding.tvName.setTextColor(ResourcesCompat.getColor(resources, R.color.color_yellow, null))
                    itemGuideTagBinding.ivChoose.setImageResource(R.drawable.xx_n_dl)
                } else {
                    itemGuideTagBinding.tvName.setTextColor(ResourcesCompat.getColor(resources, R.color.color_black, null))
                    itemGuideTagBinding.ivChoose.setImageResource(R.drawable.xx_c_dl)
                }

                itemView.setOnClickListener {
                    if (mTargetList.contains(itemData.id)) {
                        mTargetList.remove(itemData.id)
                        mTagAdapter.notifyItemChanged(position)
                    } else {
                        mTargetList.add(itemData.id)
                        mTagAdapter.notifyItemChanged(position)
                    }
                }
            }
            .create()

        binding.includeTags.rcTags.layoutManager = GridLayoutManager(this, 3)
        binding.includeTags.rcTags.adapter = mTagAdapter

        DataManager.getTags {
            mTagList.clear()
            mTagList.addAll(it)
            mTagAdapter.notifyItemRangeChanged(0, mTagList.size)
        }
    }

    private fun updateUserInfo(
        avatar: String?,
        birthday: String?,
        nickname: String?,
        isMale: Int?,
        targetIsMale: Int?,
        albums: List<String>?,
        tags: List<Int>?
    ) {
        DataManager.updateUserInfo(avatar, birthday, nickname, isMale, targetIsMale, albums, tags) {
            if (it) {
                checkStep()
            }
        }
    }

    private fun openAlbum(requestCode: Int) {
        checkReadAndWritePermissions {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, requestCode)
        }
    }

    private fun toAuthPage() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun toMainPage() {
        val intent = Intent()
        intent.setClass(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkParam() {
        ReportManager.firebaseCustomLog(firebaseAnalytics, "guide_force_leave_click", "leave guide page")
        ReportManager.appsFlyerCustomLog(this, "guide_force_leave_click", "leave guide page")

        getUserInfo { userInfo ->
            if (userInfo.sex == 0 || userInfo.age == 0 || userInfo.avatar.isBlank() || userInfo.nickname.isBlank()) {
                ToastUtil.showShort(this, "please finish your profile and enjoy more benefits")
                return@getUserInfo
            }

            val param = intent.getBooleanExtra("home", false)
            if (param) {
                toMainPage()
            } else {
                finish()
            }

            ReportManager.firebaseCustomLog(firebaseAnalytics, "guide_force_leave", "leave guide page")
            ReportManager.appsFlyerCustomLog(this, "guide_force_leave", "leave guide page")
        }
    }

    @Deprecated("")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1001) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    val realPath = FileUtil.getRealPathFromUri(this, uri)
                    if (realPath != null) {
                        CManager.compress(this, realPath, object : FileCallback {
                            override fun onSuccess(filePath: String) {
                                dialog?.show()
                                DataManager.uploadFileToOss(this@GuideActivity, filePath) {
                                    dialog?.cancel()
                                    ImageManager.getBitmap(this@GuideActivity, filePath) { bitmap ->
                                        mList.add(mList.size - 1, bitmap)
                                        mAlbumList.add(it)
                                        mAdapter.notifyItemRangeChanged(0, mList.size)
                                    }
                                }
                            }

                            override fun onFailed(message: String) {
                                ToastUtil.showShort(this@GuideActivity, message)
                            }
                        })
                    }
                }
            }
        }

        if (requestCode == 0x1002) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    val realPath = FileUtil.getRealPathFromUri(this, uri)
                    if (realPath != null) {
                        CManager.compress(this, realPath, object : FileCallback {
                            override fun onSuccess(filePath: String) {
                                dialog?.show()
                                DataManager.uploadFileToOss(this@GuideActivity, filePath) {
                                    runOnUiThread {
                                        dialog?.cancel()
                                        binding.includeAvatar.ivAvatar.visibility = View.VISIBLE
                                        binding.includeAvatar.llSettingAvatar.visibility = View.GONE
                                        mAvatar = it
                                        Glide.with(this@GuideActivity).load(it).apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.includeAvatar.ivAvatar)
                                    }
                                }
                            }

                            override fun onFailed(message: String) {
                                ToastUtil.showShort(this@GuideActivity, message)
                            }
                        })
                    }
                }
            }
        }
    }

    enum class GenderType { Male, Female }
    enum class ChooseType { I, Find }
    enum class Step { Gender, Birthday, NickName, Avatar, Album, Tags }
}