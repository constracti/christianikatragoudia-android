package gr.christianikatragoudia.app.network

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

object TheAnalytics {

    private const val EVENT_UPDATE_CHECK = "update_check"
    private const val EVENT_UPDATE_APPLY = "update_apply"
    private const val PARAM_TONALITY = "tonality"

    fun logAppOpen() {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN) {}
    }

    fun logScreenView(screenClass: String, screenName: String) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }

    fun logScreenViewWithTonality(screenClass: String, screenName: String, tonality: String) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(PARAM_TONALITY, tonality)
        }
    }

    fun logSearch(searchTerm: String) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SEARCH) {
            param(FirebaseAnalytics.Param.SEARCH_TERM, searchTerm)
        }
    }

    fun logShare(contentType: String, itemId: String) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SHARE) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            param(FirebaseAnalytics.Param.ITEM_ID, itemId)
        }
    }

    fun logUpdateCheck() {
        Firebase.analytics.logEvent(EVENT_UPDATE_CHECK) {}
    }

    fun logUpdateApply() {
        Firebase.analytics.logEvent(EVENT_UPDATE_APPLY) {}
    }
}
