package com.hianime.extension

import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.*
import eu.kanade.tachiyomi.animesource.online.ParsedAnimeHttpSource
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservable
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable

class HiAnime : ParsedAnimeHttpSource() {

    override val name = "HiAnime"
    override val baseUrl = "https://hianime.to"
    override val lang = "en"
    override val supportsLatest = true

    // Fetch popular anime
    override fun popularAnimeRequest(page: Int): Request {
        return GET("$baseUrl/popular?page=$page", headers)
    }

    override fun popularAnimeParse(response: Response): AnimePage {
        val document = response.asJsoup()
        val animeList = document.select("div.anime-list div.anime-item").map { element ->
            popularAnimeFromElement(element)
        }
        val hasNextPage = document.select("a.next-page").isNotEmpty()
        return AnimePage(animeList, hasNextPage)
    }

    private fun popularAnimeFromElement(element: Element): SAnime {
        return SAnime.create().apply {
            title = element.select("h3.title").text()
            thumbnail_url = element.select("img.thumbnail").attr("abs:src")
            setUrlWithoutDomain(element.select("a").attr("href"))
        }
    }

    // Fetch anime details
    override fun animeDetailsParse(document: Document): SAnime {
        return SAnime.create().apply {
            title = document.select("h1.title").text()
            thumbnail_url = document.select("img.thumbnail").attr("abs:src")
            description = document.select("div.description").text()
        }
    }

    // Fetch episode list
    override fun episodeListParse(response: Response): List<SEpisode> {
        val document = response.asJsoup()
        return document.select("div.episode-list div.episode-item").map { element ->
            SEpisode.create().apply {
                name = element.select("h4.title").text()
                episode_number = element.select("span.episode-number").text().toFloatOrNull() ?: 0f
                date_upload = System.currentTimeMillis()
                setUrlWithoutDomain(element.select("a").attr("href"))
            }
        }
    }

    // Fetch video URLs
    override fun videoListParse(response: Response): List<Video> {
        val document = response.asJsoup()
        return document.select("div.video-source").map { element ->
            Video(element.select("source").attr("src"), element.select("source").attr("type"), element.select("source").attr("src"))
        }
    }

    // Other required methods (search, latest updates, etc.)
    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request {
        return GET("$baseUrl/search?q=$query&page=$page", headers)
    }

    override fun searchAnimeParse(response: Response): AnimePage {
        val document = response.asJsoup()
        val animeList = document.select("div.search-results div.anime-item").map { element ->
            searchAnimeFromElement(element)
        }
        val hasNextPage = document.select("a.next-page").isNotEmpty()
        return AnimePage(animeList, hasNextPage)
    }

    private fun searchAnimeFromElement(element: Element): SAnime {
        return SAnime.create().apply {
            title = element.select("h3.title").text()
            thumbnail_url = element.select("img.thumbnail").attr("abs:src")
            setUrlWithoutDomain(element.select("a").attr("href"))
        }
    }

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl/latest?page=$page", headers)
    }

    override fun latestUpdatesParse(response: Response): AnimePage {
        val document = response.asJsoup()
        val animeList = document.select("div.latest-updates div.anime-item").map { element ->
            popularAnimeFromElement(element)
        }
        val hasNextPage = document.select("a.next-page").isNotEmpty()
        return AnimePage(animeList, hasNextPage)
    }
}
