package eu.kanade.tachiyomi.extension.en.hianime

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SEpisode
import eu.kanade.tachiyomi.source.model.SAnime
import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup

class HiAnime : HttpSource() {
    override val name = "HiAnime"
    override val baseUrl = "https://www.hianime.to"
    override val lang = "en"
    override val supportsLatest = true

    override fun popularAnimeRequest(page: Int): Request {
        return GET("$baseUrl/videos/anime/popular", headers)
    }

    override fun popularAnimeParse(response: Response): AnimesPage {
        val document = Jsoup.parse(response.body.string())
        val animes = document.select("div.portrait-element").map {
            SAnime.create().apply {
                title = it.select("a.title").text()
                url = it.select("a").attr("href")
                thumbnail_url = it.select("img").attr("src")
            }
        }
        return AnimesPage(animes, hasNextPage = false)
    }

    override fun animeDetailsRequest(anime: SAnime): Request {
        return GET(baseUrl + anime.url, headers)
    }

    override fun animeDetailsParse(response: Response): SAnime {
        val document = Jsoup.parse(response.body.string())
        return SAnime.create().apply {
            title = document.select("h1.title").text()
            author = document.select("span.author").text()
            description = document.select("div.description").text()
            genre = document.select("span.genre").joinToString { it.text() }
            status = SAnime.UNKNOWN
            thumbnail_url = document.select("img.thumbnail").attr("src")
        }
    }

    override fun episodeListRequest(anime: SAnime): Request {
        return GET(baseUrl + anime.url, headers)
    }

    override fun episodeListParse(response: Response): List<SEpisode> {
        val document = Jsoup.parse(response.body.string())
        return document.select("div.episode-row").map {
            SEpisode.create().apply {
                name = it.select("a.title").text()
                url = it.select("a").attr("href")
                date_upload = it.select("span.date").text().toLong()
            }
        }
    }

    override fun videoListRequest(episode: SEpisode): Request {
        return GET(baseUrl + episode.url, headers)
    }

    override fun videoListParse(response: Response): List<Page> {
        val document = Jsoup.parse(response.body.string())
        return document.select("div.video-player").mapIndexed { index, element ->
            Page(index, "", element.select("source").attr("src"))
        }
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException("Not used.")
    }

    override fun getFilterList(): FilterList {
        return FilterList()
    }

    override fun searchAnimeRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$baseUrl/search?q=$query", headers)
    }

    override fun searchAnimeParse(response: Response): AnimesPage {
        val document = Jsoup.parse(response.body.string())
        val animes = document.select("div.portrait-element").map {
            SAnime.create().apply {
                title = it.select("a.title").text()
                url = it.select("a").attr("href")
                thumbnail_url = it.select("img").attr("src")
            }
        }
        return AnimesPage(animes, hasNextPage = false)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl/videos/anime/updated", headers)
    }

    override fun latestUpdatesParse(response: Response): AnimesPage {
        val document = Jsoup.parse(response.body.string())
        val animes = document.select("div.portrait-element").map {
            SAnime.create().apply {
                title = it.select("a.title").text()
                url = it.select("a").attr("href")
                thumbnail_url = it.select("img").attr("src")
            }
        }
        return AnimesPage(animes, hasNextPage = false)
    }
}
