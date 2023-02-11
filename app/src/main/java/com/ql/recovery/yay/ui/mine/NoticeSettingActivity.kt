package com.ql.recovery.yay.ui.mine

import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityNoticeSettingBinding
import com.ql.recovery.yay.ui.base.BaseActivity

class NoticeSettingActivity : BaseActivity() {
    private lateinit var binding: ActivityNoticeSettingBinding
    private var callPush = true
    private var chatPush = true
    private var fansPush = true
    private var onlinePush = true

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityNoticeSettingBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        setStatusBarLight()
        binding.includeTitle.ivBack.setOnClickListener { finish() }
        binding.ivVideoChoose.setOnClickListener { choose(NoticeType.Video) }
        binding.ivMessageChoose.setOnClickListener { choose(NoticeType.Message) }
        binding.ivAdditionChoose.setOnClickListener { choose(NoticeType.Addition) }
        binding.ivFollowChoose.setOnClickListener { choose(NoticeType.Follow) }
    }

    override fun initData() {
        binding.includeTitle.tvName.text = getString(R.string.setting_notice)
        loadInfo()
    }

    private fun loadInfo() {
        DataManager.getPushSetting { push ->
            callPush = push.call_push
            chatPush = push.chat_push
            fansPush = push.new_fans_push
            onlinePush = push.online_push

            if (push.call_push) {
                binding.ivVideoChoose.setImageResource(R.drawable.filter_open)
            } else {
                binding.ivVideoChoose.setImageResource(R.drawable.filter_close)
            }

            if (push.chat_push) {
                binding.ivMessageChoose.setImageResource(R.drawable.filter_open)
            } else {
                binding.ivMessageChoose.setImageResource(R.drawable.filter_close)
            }

            if (push.new_fans_push) {
                binding.ivAdditionChoose.setImageResource(R.drawable.filter_open)
            } else {
                binding.ivAdditionChoose.setImageResource(R.drawable.filter_close)
            }

            if (push.online_push) {
                binding.ivFollowChoose.setImageResource(R.drawable.filter_open)
            } else {
                binding.ivFollowChoose.setImageResource(R.drawable.filter_close)
            }
        }
    }

    private fun choose(type: NoticeType) {
        when (type) {
            NoticeType.Video -> {
                callPush = !callPush
                if (!callPush) {
                    binding.ivVideoChoose.setImageResource(R.drawable.filter_close)
                } else {
                    binding.ivVideoChoose.setImageResource(R.drawable.filter_open)
                }

                DataManager.updatePushSetting(callPush, null, null, null) {
                    if (!it) {
                        callPush = !callPush
                        if (callPush) {
                            binding.ivVideoChoose.setImageResource(R.drawable.filter_close)
                        } else {
                            binding.ivVideoChoose.setImageResource(R.drawable.filter_open)
                        }
                    }
                }
            }

            NoticeType.Message -> {
                chatPush = !chatPush
                if (!chatPush) {
                    binding.ivMessageChoose.setImageResource(R.drawable.filter_close)
                } else {
                    binding.ivMessageChoose.setImageResource(R.drawable.filter_open)
                }

                DataManager.updatePushSetting(null, chatPush, null, null) {
                    if (!it) {
                        chatPush = !chatPush
                        if (!chatPush) {
                            binding.ivMessageChoose.setImageResource(R.drawable.filter_close)
                        } else {
                            binding.ivMessageChoose.setImageResource(R.drawable.filter_open)
                        }
                    }
                }
            }

            NoticeType.Addition -> {
                fansPush = !fansPush
                if (!fansPush) {
                    binding.ivAdditionChoose.setImageResource(R.drawable.filter_close)
                } else {
                    binding.ivAdditionChoose.setImageResource(R.drawable.filter_open)
                }

                DataManager.updatePushSetting(null, null, fansPush, null) {
                    if (!it) {
                        fansPush = !fansPush
                        if (!fansPush) {
                            binding.ivAdditionChoose.setImageResource(R.drawable.filter_close)
                        } else {
                            binding.ivAdditionChoose.setImageResource(R.drawable.filter_open)
                        }
                    }
                }
            }

            NoticeType.Follow -> {
                onlinePush = !onlinePush
                if (!onlinePush) {
                    binding.ivFollowChoose.setImageResource(R.drawable.filter_close)
                } else {
                    binding.ivFollowChoose.setImageResource(R.drawable.filter_open)
                }

                DataManager.updatePushSetting(null, null, null, onlinePush) {
                    if (!it) {
                        onlinePush = !onlinePush
                        if (!onlinePush) {
                            binding.ivFollowChoose.setImageResource(R.drawable.filter_close)
                        } else {
                            binding.ivFollowChoose.setImageResource(R.drawable.filter_open)
                        }
                    }
                }
            }
        }
    }

    private enum class NoticeType { Video, Message, Addition, Follow }
}