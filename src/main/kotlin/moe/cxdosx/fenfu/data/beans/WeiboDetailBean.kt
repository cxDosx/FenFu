package moe.cxdosx.fenfu.data.beans

data class WeiboDetailBean(
    val `data`: Data,
    val ok: Int
)

data class Data(
    val banners: Any,
    val cardlistInfo: CardlistInfo,
    val cards: List<Card>,
    val scheme: String,
    val showAppTips: Int
)

data class CardlistInfo(
    val containerid: String,
    val show_style: Int,
    val since_id: Long,
    val total: Int,
    val v_p: Int
)

data class Card(
    val card_group: List<CardGroup>,
    val card_style: Int,
    val card_type: Int,
    val display_arrow: Int,
    val itemid: String,
    val mblog: Mblog?,
    val scheme: String,
    val show_type: Int,
    val skip_group_title: Boolean
)

data class CardGroup(
    val card_type: Int,
    val col: Int,
    val group: List<Group>
)

data class Mblog(
    val alchemy_params: AlchemyParams,
    val attitudes_count: Int,
    val bid: String,
    val bmiddle_pic: String,
    val can_edit: Boolean,
    val cardid: String,
    val comments_count: Int,
    val content_auth: Int,
    val created_at: String,
    val edit_at: String,
    val edit_config: EditConfig,
    val edit_count: Int,
    val enable_comment_guide: Boolean,
    val extern_safe: Int,
    val favorited: Boolean,
    val hide_flag: Int,
    val id: String,
    val idstr: String,
    val isLongText: Boolean,
    val isTop: Int,
    val is_imported_topic: Boolean,
    val is_paid: Boolean,
    val mblog_menu_new_style: Int,
    val mblog_vip_type: Int,
    val mblogtype: Int,
    val mid: String,
    val mlevel: Int,
    val more_info_type: Int,
    val number_display_strategy: NumberDisplayStrategy,
    val original_pic: String,
    val page_info: PageInfo,
    val pending_approval_count: Int,
    val pic_num: Int,
    val pic_types: String,
    val pics: List<Pic>,
    val raw_text: String,
    val repost_type: Int,
    val reposts_count: Int,
    val retweeted_status: RetweetedStatus?,
    val reward_exhibition_type: Int,
    val show_additional_indication: Int,
    val show_attitude_bar: Int,
    val source: String,
    val sync_mblog: Boolean,
    val text: String,
    val textLength: Int,
    val thumbnail_pic: String,
    val title: Title,
    val topic_id: String,
    val user: UserX,
    val version: Int,
    val visible: VisibleX,
    val weibo_position: Int
)

data class Group(
    val action_log: ActionLog,
    val icon: String,
    val scheme: String,
    val title_sub: String,
    val word_scheme: String
)

data class ActionLog(
    val act_code: Int,
    val ext: String,
    val fid: String,
    val oid: String
)

data class AlchemyParams(
    val ug_red_envelope: Boolean
)

data class EditConfig(
    val edited: Boolean,
    val menu_edit_history: MenuEditHistory
)

data class NumberDisplayStrategy(
    val apply_scenario_flag: Int,
    val display_text: String,
    val display_text_min_number: Int
)

data class PageInfo(
    val content1: String,
    val content2: String,
    val icon: String,
    val object_id: String,
    val page_pic: PagePic,
    val page_title: String,
    val page_url: String,
    val type: String
)

data class Pic(
    val geo: Geo,
    val large: Large,
    val pid: String,
    val size: String,
    val url: String
)

data class RetweetedStatus(
    val attitudes_count: Int,
    val bid: String,
    val bmiddle_pic: String,
    val can_edit: Boolean,
    val cardid: String,
    val comments_count: Int,
    val content_auth: Int,
    val created_at: String,
    val edit_config: EditConfigX,
    val favorited: Boolean,
    val hide_flag: Int,
    val id: String,
    val idstr: String,
    val isLongText: Boolean,
    val is_paid: Boolean,
    val mblog_vip_type: Int,
    val mblogtype: Int,
    val mid: String,
    val mlevel: Int,
    val more_info_type: Int,
    val number_display_strategy: NumberDisplayStrategyX,
    val original_pic: String,
    val page_info: PageInfoX,
    val pending_approval_count: Int,
    val picStatus: String,
    val pic_num: Int,
    val pic_types: String,
    val pics: List<PicX>,
    val raw_text: String,
    val reposts_count: Int,
    val retweeted: Int,
    val reward_exhibition_type: Int,
    val safe_tags: Int,
    val show_additional_indication: Int,
    val show_attitude_bar: Int,
    val source: String,
    val text: String,
    val textLength: Int,
    val thumbnail_pic: String,
    val user: User,
    val version: Int,
    val visible: Visible,
    val weibo_position: Int
)

data class Title(
    val base_color: Int,
    val text: String
)

data class UserX(
    val avatar_hd: String,
    val badge: BadgeX,
    val close_blue_v: Boolean,
    val cover_image_phone: String,
    val description: String,
    val follow_count: Int,
    val follow_me: Boolean,
    val followers_count: Int,
    val following: Boolean,
    val gender: String,
    val id: Long,
    val like: Boolean,
    val like_me: Boolean,
    val mbrank: Int,
    val mbtype: Int,
    val profile_image_url: String,
    val profile_url: String,
    val screen_name: String,
    val statuses_count: Int,
    val urank: Int,
    val verified: Boolean,
    val verified_reason: String,
    val verified_type: Int,
    val verified_type_ext: Int
)

data class VisibleX(
    val list_id: Int,
    val type: Int
)

data class MenuEditHistory(
    val scheme: String,
    val title: String
)

data class PagePic(
    val url: String
)

data class Geo(
    val croped: Boolean,
    val height: Int,
    val width: Int
)

data class Large(
    val geo: GeoX,
    val size: String,
    val url: String
)

data class GeoX(
    val croped: Boolean,
    val height: String,
    val width: String
)

data class EditConfigX(
    val edited: Boolean
)

data class NumberDisplayStrategyX(
    val apply_scenario_flag: Int,
    val display_text: String,
    val display_text_min_number: Int
)

data class PageInfoX(
    val content1: String,
    val content2: String,
    val icon: String,
    val object_id: String,
    val page_pic: PagePicX,
    val page_title: String,
    val page_url: String,
    val type: String
)

data class PicX(
    val geo: GeoXX,
    val large: LargeX,
    val pid: String,
    val size: String,
    val url: String
)

data class User(
    val avatar_hd: String,
    val badge: Badge,
    val close_blue_v: Boolean,
    val cover_image_phone: String,
    val description: String,
    val follow_count: Int,
    val follow_me: Boolean,
    val followers_count: Int,
    val following: Boolean,
    val gender: String,
    val id: Long,
    val like: Boolean,
    val like_me: Boolean,
    val mbrank: Int,
    val mbtype: Int,
    val profile_image_url: String,
    val profile_url: String,
    val screen_name: String,
    val statuses_count: Int,
    val urank: Int,
    val verified: Boolean,
    val verified_reason: String,
    val verified_type: Int,
    val verified_type_ext: Int
)

data class Visible(
    val list_id: Int,
    val type: Int
)

data class PagePicX(
    val url: String
)

data class GeoXX(
    val croped: Boolean,
    val height: Int,
    val width: Int
)

data class LargeX(
    val geo: GeoXXX,
    val size: String,
    val url: String
)

data class GeoXXX(
    val croped: Boolean,
    val height: String,
    val width: String
)

data class Badge(
    val dzwbqlx_2016: Int,
    val user_name_certificate: Int
)

data class BadgeX(
    val dzwbqlx_2016: Int,
    val user_name_certificate: Int
)