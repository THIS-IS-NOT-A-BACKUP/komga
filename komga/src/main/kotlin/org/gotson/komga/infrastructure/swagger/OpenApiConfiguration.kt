package org.gotson.komga.infrastructure.swagger

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.servers.ServerVariable
import io.swagger.v3.oas.models.servers.ServerVariables
import io.swagger.v3.oas.models.tags.Tag
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.ANNOUNCEMENTS
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.API_KEYS
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.BOOKS
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.BOOK_FONTS
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.BOOK_IMPORT
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.BOOK_PAGES
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.BOOK_POSTER
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.BOOK_WEBPUB
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.CLAIM
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.CLIENT_SETTINGS
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.COLLECTIONS
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.COLLECTION_POSTER
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.COLLECTION_SERIES
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.COMICRACK
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.CURRENT_USER
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.DEPRECATED
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.DUPLICATE_PAGES
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.FILE_SYSTEM
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.HISTORY
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.LIBRARIES
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.MIHON
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.OAUTH2
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.READLISTS
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.READLIST_BOOKS
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.READLIST_POSTER
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.REFERENTIAL
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.RELEASES
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.SERIES
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.SERIES_POSTER
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.SERVER_SETTINGS
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.SYNCPOINTS
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.TASKS
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.USERS
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration.TagNames.USER_SESSION
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(
  @Value("\${application.version}") private val appVersion: String,
) {
  @Bean
  fun openApi(): OpenAPI =
    OpenAPI()
      .info(
        Info()
          .title("Komga API")
          .version(appVersion)
          .description(
            """
            Komga RESTful API.

            ## Authentication

            Most endpoints require authentication. Authentication is done using either:
            - Basic Authentication
            - Passing an API Key in the `X-API-Key` header

            ## Sessions

            Upon successful authentication, a session is created, and can be reused.

            - By default, a `SESSION` cookie is set via `Set-Cookie` response header. This works well for browsers and clients that can handle cookies.
            - If you specify a header `X-Auth-Token` during authentication, the session ID will be returned via this same header. You can then pass that header again for subsequent requests to reuse the session.

            If you need to set the session cookie later on, you can call `/api/v1/login/set-cookie` with `X-Auth-Token`. The response will contain the `Set-Cookie` header.

            ## Remember Me

            During authentication, if a request parameter `remember-me` is passed and set to `true`, the server will also return a `remember-me` cookie. This cookie will be used to login automatically even if the session has expired.

            ## Logout

            You can explicitly logout an existing session by calling `/api/logout`. This would return a `204`.

            ## Deprecation

            API endpoints marked as deprecated will be removed in the next major version.
            """.trimIndent(),
          ).license(License().name("MIT").url("https://github.com/gotson/komga/blob/master/LICENSE")),
      ).externalDocs(
        ExternalDocumentation()
          .description("Komga documentation")
          .url("https://komga.org"),
      ).components(
        Components()
          .addSecuritySchemes(
            "basicAuth",
            SecurityScheme()
              .type(SecurityScheme.Type.HTTP)
              .scheme("basic"),
          ).addSecuritySchemes(
            "apiKey",
            SecurityScheme()
              .type(SecurityScheme.Type.APIKEY)
              .`in`(SecurityScheme.In.HEADER)
              .name("X-API-Key"),
          ),
      ).tags(tags)
      .extensions(mapOf("x-tagGroups" to tagGroups))
      .servers(
        listOf(
          Server()
            .url("http://localhost:{port}")
            .description("Local development server")
            .variables(
              ServerVariables()
                .addServerVariable(
                  "port",
                  ServerVariable()
                    .addEnumItem("8080")
                    .addEnumItem("25600"),
                ),
            ),
          Server()
            .url("https://demo.komga.org")
            .description("Demo server"),
        ),
      ).path(
        "/api/logout",
        PathItem()
          .summary("Logout current session")
          .get(logoutOperation.operationId("getLogout"))
          .post(logoutOperation.operationId("postLogout")),
      )

  private val logoutOperation =
    Operation()
      .tags(listOf(USER_SESSION))
      .summary("Logout")
      .description("Invalidates the current session and clean up any remember-me authentication.")
      .responses(ApiResponses().addApiResponse("204", ApiResponse().description("No Content")))

  data class TagGroup(
    val name: String,
    val tags: List<String>,
  )

  private val tagGroups =
    listOf(
      TagGroup(
        "Libraries",
        listOf(
          LIBRARIES,
        ),
      ),
      TagGroup(
        "Series",
        listOf(
          SERIES,
          SERIES_POSTER,
        ),
      ),
      TagGroup(
        "Books",
        listOf(
          BOOKS,
          BOOK_PAGES,
          BOOK_POSTER,
          BOOK_IMPORT,
          DUPLICATE_PAGES,
          BOOK_WEBPUB,
          BOOK_FONTS,
        ),
      ),
      TagGroup(
        "Collections",
        listOf(
          COLLECTIONS,
          COLLECTION_SERIES,
          COLLECTION_POSTER,
        ),
      ),
      TagGroup(
        "Readlists",
        listOf(
          READLISTS,
          READLIST_BOOKS,
          READLIST_POSTER,
        ),
      ),
      TagGroup(
        "Referential",
        listOf(
          REFERENTIAL,
        ),
      ),
      TagGroup(
        "Users",
        listOf(
          CURRENT_USER,
          USERS,
          API_KEYS,
          USER_SESSION,
          OAUTH2,
          SYNCPOINTS,
        ),
      ),
      TagGroup(
        "Server",
        listOf(
          CLAIM,
          SERVER_SETTINGS,
          TASKS,
          HISTORY,
          FILE_SYSTEM,
          RELEASES,
          ANNOUNCEMENTS,
        ),
      ),
      TagGroup(
        "Integrations",
        listOf(
          CLIENT_SETTINGS,
          MIHON,
          COMICRACK,
        ),
      ),
      TagGroup(
        "Deprecation",
        listOf(
          DEPRECATED,
        ),
      ),
    )

  object TagNames {
    const val LIBRARIES = "Libraries"

    const val SERIES = "Series"
    const val SERIES_POSTER = "Series Poster"

    const val BOOKS = "Books"
    const val BOOK_POSTER = "Book Poster"
    const val BOOK_PAGES = "Book Pages"
    const val BOOK_WEBPUB = "WebPub Manifest"
    const val BOOK_IMPORT = "Import"
    const val BOOK_FONTS = "Fonts"
    const val DUPLICATE_PAGES = "Duplicate Pages"

    const val COLLECTIONS = "Collections"
    const val COLLECTION_SERIES = "Collection Series"
    const val COLLECTION_POSTER = "Collection Poster"

    const val READLISTS = "Readlists"
    const val READLIST_BOOKS = "Readlist Books"
    const val READLIST_POSTER = "Readlist Poster"

    const val REFERENTIAL = "Referential metadata"

    const val CURRENT_USER = "Current user"
    const val USERS = "Users"
    const val API_KEYS = "API Keys"
    const val USER_SESSION = "User session"
    const val OAUTH2 = "OAuth2"
    const val SYNCPOINTS = "Sync points"

    const val CLAIM = "Claim server"
    const val TASKS = "Tasks"
    const val HISTORY = "History"
    const val FILE_SYSTEM = "File system"
    const val SERVER_SETTINGS = "Server settings"
    const val RELEASES = "Releases"
    const val ANNOUNCEMENTS = "Announcements"

    const val MIHON = "Mihon"
    const val COMICRACK = "ComicRack"
    const val CLIENT_SETTINGS = "Client settings"

    const val DEPRECATED = "Deprecated"
  }

  private val tags =
    listOf(
      Tag().name(LIBRARIES).description("Manage libraries."),
      Tag().name(SERIES).description("Manage series."),
      Tag().name(SERIES_POSTER).description("Manage posters for series."),
      Tag().name(BOOKS).description("Manage books."),
      Tag().name(BOOK_POSTER).description("Manage posters for books."),
      Tag().name(BOOK_PAGES),
      Tag().name(BOOK_WEBPUB),
      Tag().name(BOOK_IMPORT),
      Tag().name(BOOK_FONTS).description("Provide font files and CSS for the Epub Reader."),
      Tag().name(DUPLICATE_PAGES).description("Manage duplicate pages. Duplicate pages are identified by a page hash."),
      Tag().name(COLLECTIONS).description("Manage collections."),
      Tag().name(COLLECTION_POSTER).description("Manage posters for collections."),
      Tag().name(COLLECTION_SERIES),
      Tag().name(READLISTS).description("Manage readlists."),
      Tag().name(READLIST_POSTER).description("Manage posters for readlists."),
      Tag().name(READLIST_BOOKS),
      Tag().name(REFERENTIAL).description("Retrieve referential metadata from all items in the Komga server."),
      Tag().name(CURRENT_USER).description("Manage current user."),
      Tag().name(USERS).description("Manage users."),
      Tag().name(API_KEYS).description("Manage API Keys"),
      Tag().name(USER_SESSION),
      Tag().name(OAUTH2).description("List registered OAuth2 providers"),
      Tag().name(SYNCPOINTS).description("Sync points are automatically created during a Kobo sync."),
      Tag().name(CLAIM).description("Claim a freshly installed Komga server."),
      Tag().name(TASKS).description("Manage server tasks"),
      Tag().name(HISTORY).description("Server events history"),
      Tag().name(FILE_SYSTEM).description("List files from the host server's file system"),
      Tag().name(SERVER_SETTINGS).description("Store and retrieve server settings"),
      Tag().name(RELEASES).description("Retrieve releases information"),
      Tag().name(ANNOUNCEMENTS).description("Retrieve announcements from the Komga website"),
      Tag().name(MIHON),
      Tag().name(COMICRACK),
      Tag().name(CLIENT_SETTINGS).description("Store and retrieve global and per-user settings. Those settings are not used by Komga itself, but can be stored for convenience by client applications."),
      Tag().name(DEPRECATED),
    )
}
