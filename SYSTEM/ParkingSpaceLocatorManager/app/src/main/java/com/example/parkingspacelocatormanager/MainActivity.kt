package com.example.parkingspacelocatormanager

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import android.app.DatePickerDialog
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private val navy = Color.rgb(5, 11, 27)
    private val panel = Color.rgb(17, 29, 57)
    private val blue = Color.rgb(0, 145, 245)
    private val cyan = Color.rgb(25, 213, 233)
    private val white = Color.WHITE
    private val muted = Color.rgb(139, 155, 185)
    private val green = Color.rgb(24, 214, 154)
    private val red = Color.rgb(255, 60, 112)
    private val yellow = Color.rgb(255, 212, 59)

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var nav: LinearLayout

    private var currentScreen: String? = null
    private val screenHistory = mutableListOf<String>()
    private var restoringScreen = false

    private var poblacion = 3
    private var spot = 3

    data class Booking(
        val spot: Int,
        val barangay: Int,
        val date: String,
        val time: String,
        val hours: Int,
        var status: String
    )

    private val bookings = mutableListOf(
        Booking(11, 1, "2026-10-22", "02:00 PM", 2, "COMPLETED"),
        Booking(5, 5, "2026-10-18", "09:00 AM", 4, "CANCELLED")
    )

    private fun dp(n: Int) =
        (n * resources.displayMetrics.density).toInt()

    private fun bg(color: Int, radius: Int = 12): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = navy
        window.navigationBarColor = navy

        // AndroidX back navigation handler
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    goBack()
                }
            }
        )

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(navy)
        }

        setContentView(root)
        welcome()
    }

    private fun trackScreen(screen: String) {
        if (restoringScreen) {
            currentScreen = screen
            restoringScreen = false
            return
        }

        if (currentScreen != null && currentScreen != screen) {
            screenHistory.add(currentScreen!!)
        }

        currentScreen = screen
    }

    private fun displayScreen(screen: String) {
        when (screen) {
            "welcome" -> welcome()
            "login" -> login()
            "register" -> register()
            "home" -> home()
            "map" -> map()
            "spotDetails" -> spotDetails()
            "reserve" -> reserve()
            "confirmation" -> confirmation()
            "bookings" -> bookings(false)
            "profile" -> profile()
            else -> welcome()
        }
    }

    private fun goBack() {
        if (screenHistory.isNotEmpty()) {
            val previous = screenHistory.removeAt(screenHistory.lastIndex)
            restoringScreen = true
            displayScreen(previous)
        } else if (currentScreen != "welcome") {
            restoringScreen = true
            welcome()
        }
    }

    private fun setup(showNav: Boolean = true) {
        root.removeAllViews()

        val scroll = ScrollView(this).apply {
            setFillViewport(true)
            isVerticalScrollBarEnabled = false
            setBackgroundColor(navy)
        }

        content = LinearLayout(this).apply {git rm -r --cached .

            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(25))
        }

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, dp(5))
            setBackgroundColor(Color.rgb(8, 15, 35))
        }

        if (showNav) {
            val items = listOf(
                "⌂\nHome",
                "◇\nMap",
                "▦\nBookings",
                "♙\nProfile"
            )

            items.forEachIndexed { index, item ->
                val t = text(
                    item,
                    11f,
                    if (index == 0) cyan else muted,
                    true
                )

                t.gravity = Gravity.CENTER

                nav.addView(
                    t,
                    LinearLayout.LayoutParams(0, dp(55), 1f)
                )

                t.setOnClickListener {
                    when (index) {
                        0 -> home()
                        1 -> map()
                        2 -> bookings(false)
                        3 -> profile()
                    }
                }
            }

            root.addView(nav, LinearLayout.LayoutParams(-1, dp(60)))
        }
    }

    private fun text(
        value: String,
        size: Float = 14f,
        color: Int = white,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    private fun title(value: String, size: Float = 22f) {
        val t = text(value, size, white, true)

        if (value.trimStart().startsWith("‹")) {
            t.isClickable = true
            t.setOnClickListener { goBack() }
        }

        val p = LinearLayout.LayoutParams(-1, -2)
        p.topMargin = dp(8)
        content.addView(t, p)
    }

    private fun subtitle(value: String) {
        val t = text(value, 11f, muted)
        val p = LinearLayout.LayoutParams(-1, -2)
        p.topMargin = dp(5)
        content.addView(t, p)
    }

    private fun space(height: Int) {
        content.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun input(
        hint: String,
        password: Boolean = false
    ): EditText {
        val e = EditText(this).apply {
            this.hint = hint
            textSize = 13f
            setTextColor(white)
            setHintTextColor(muted)
            background = bg(panel)
            setPadding(dp(14), 0, dp(14), 0)
            isSingleLine = true
            if (password) inputType = 129
        }

        val p = LinearLayout.LayoutParams(-1, dp(48))
        p.topMargin = dp(10)
        content.addView(e, p)
        return e
    }

    private fun button(
        value: String,
        action: () -> Unit,
        primary: Boolean = true
    ) {
        val b = Button(this).apply {
            text = value
            textSize = 13f
            isAllCaps = false
            setTextColor(white)
            background = bg(
                if (primary) blue else Color.rgb(23, 37, 68),
                30
            )
            setOnClickListener { action() }
        }

        val p = LinearLayout.LayoutParams(-1, dp(48))
        p.topMargin = dp(12)
        content.addView(b, p)
    }

    private fun card(): LinearLayout {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), dp(12), dp(13), dp(12))
            background = bg(panel)
        }

        val p = LinearLayout.LayoutParams(-1, -2)
        p.topMargin = dp(10)
        content.addView(c, p)
        return c
    }

    private fun cardText(
        parent: LinearLayout,
        value: String,
        size: Float = 12f,
        color: Int = white,
        bold: Boolean = false
    ) {
        val t = text(value, size, color, bold)
        val p = LinearLayout.LayoutParams(-1, -2)
        p.topMargin = dp(4)
        parent.addView(t, p)
    }

    private fun toast(value: String) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show()
    }

    // WELCOME SCREEN

    private fun welcome() {
        trackScreen("welcome")
        setup(false)
        space(75)

        val logo = text("P", 38f, cyan, true).apply {
            gravity = Gravity.CENTER
            background = bg(panel, 50)
        }

        content.addView(
            logo,
            LinearLayout.LayoutParams(dp(82), dp(82)).apply {
                gravity = Gravity.CENTER
            }
        )

        val brand = text("PARKMATE", 28f, white, true).apply {
            gravity = Gravity.CENTER
        }
        content.addView(brand)

        val subtitleText = text(
            "Parking Space Locator & Manager",
            12f,
            muted
        ).apply {
            gravity = Gravity.CENTER
        }
        content.addView(subtitleText)

        space(45)
        title("Welcome!", 25f)
        subtitle("Please sign in to continue, or create an account if you are new.")

        space(20)
        button("SIGN IN", { login() })
        button("CREATE ACCOUNT", { register() }, false)

        space(30)

        val note = text(
            "Find and reserve your parking space with ParkMate.",
            11f,
            muted
        ).apply {
            gravity = Gravity.CENTER
        }

        content.addView(note)
    }

    // LOGIN SCREEN

    private fun login() {
        trackScreen("login")
        setup(false)
        space(55)

        val logo = text("P", 35f, cyan, true).apply {
            gravity = Gravity.CENTER
            background = bg(panel, 50)
        }

        content.addView(
            logo,
            LinearLayout.LayoutParams(dp(75), dp(75)).apply {
                gravity = Gravity.CENTER
            }
        )

        val brand = text("C&I PARKFINDER", 27f, white, true)
        brand.gravity = Gravity.CENTER
        content.addView(brand)

        val sub = text("Parking Space Locator & Manager", 12f, muted)
        sub.gravity = Gravity.CENTER
        content.addView(sub)

        space(30)

        val email = input("✉  Email or Phone Number")
        val pass = input("♙  Password", true)

        val remember = CheckBox(this).apply {
            text = "Remember me"
            textSize = 11f
            setTextColor(muted)
            isChecked = true
        }
        content.addView(remember)

        val forgot = text("Forgot password?", 11f, cyan)
        forgot.gravity = Gravity.RIGHT
        content.addView(forgot)

        button("➜  LOG IN", {
            if (email.text.isBlank() || pass.text.isBlank()) {
                toast("Enter your email and password")
            } else {
                home()
            }
        })

        val signup = text(
            "Don't have an account? Create an account",
            11f,
            cyan
        )
        signup.gravity = Gravity.CENTER

        val p = LinearLayout.LayoutParams(-1, -2)
        p.topMargin = dp(90)
        content.addView(signup, p)

        signup.setOnClickListener { register() }

        val location = text("⌖ Nasugbu, Batangas", 10f, muted)
        location.gravity = Gravity.CENTER
        content.addView(location)
    }

    // CREATE ACCOUNT

    private fun register() {
        trackScreen("register")
        setup(false)

        title("Create Account", 24f)
        subtitle("Join ParkMate and secure your spot today.")

        val name = input("Full Name")
        val email = input("Email Address")
        val phone = input("Phone Number")
        val pass = input("Password", true)
        val confirm = input("Confirm Password", true)

        val terms = CheckBox(this).apply {
            text = "I agree to the Terms of Service & Privacy Policy"
            textSize = 10f
            setTextColor(muted)
        }
        content.addView(terms)

        button("SIGN UP", {
            when {
                name.text.isBlank() -> toast("Enter your name")
                email.text.isBlank() -> toast("Enter your email")
                phone.text.isBlank() -> toast("Enter your phone number")
                pass.text.isBlank() -> toast("Enter a password")
                pass.text.toString() != confirm.text.toString() ->
                    toast("Passwords do not match")
                !terms.isChecked -> toast("Accept the terms first")
                else -> {
                    toast("Account created!")
                    login()
                }
            }
        })

        val back = text("Already have an account? Log in", 11f, cyan)
        back.gravity = Gravity.CENTER
        content.addView(back)
        back.setOnClickListener { login() }
    }

    // HOME SCREEN

    private fun home() {
        trackScreen("home")
        setup()

        title("▣ ParkMate", 20f)
        subtitle("Hello, Driver!")

        title("Where do you want to park?", 21f)

        val search = input("⌕ Search Poblacion or Barangay...")

        val promo = card()
        cardText(promo, "⚡ 50 Free Spots Nearby", 14f, white, true)
        cardText(promo, "Across Poblacion 1 to 12", 10f, muted)

        title("Select Barangay", 14f)

        val grid = GridLayout(this).apply {
            columnCount = 2
        }

        val counts = listOf(8, 3, 14, 0, 5, 11, 6, 9, 4, 7, 10, 12)

        for (i in 1..12) {
            val count = counts[i - 1]

            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(10), dp(12), dp(5), dp(12))
                background = bg(panel)

                setOnClickListener {
                    poblacion = i
                    map()
                }
            }

            tile.addView(text("Poblacion $i  ›", 12f, white, true))

            tile.addView(
                text(
                    if (count == 0) "● No available space"
                    else "● $count listed locations",
                    10f,
                    if (count == 0) red else green
                )
            )

            val p = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(70)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(4), dp(4), dp(4), dp(4))
            }

            grid.addView(tile, p)
        }

        content.addView(grid)

        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                for (i in 0 until grid.childCount) {
                    grid.getChildAt(i).visibility =
                        if (
                            s.isNullOrBlank() ||
                            "Poblacion ${i + 1}".contains(s.toString(), true)
                        ) View.VISIBLE else View.GONE
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    // MAP SCREEN

    private fun map() {
        trackScreen("map")
        setup()

        title("‹    Parking Map", 20f)
        subtitle("Nasugbu, Batangas • Your listed locations")

        val locationsByBarangay = mapOf(
            1 to listOf(
                "Osna",
                "McDonald's",
                "Simbahan",
                "La Locanda Ristorante Pizzeria"
            ),
            2 to listOf(
                "Jollibee",
                "McDonald's",
                "Simbahan",
                "L'Bulalo Restaurant",
                "Nasugbu Park (Plaza)",
                "Ryza's Kitchenette"
            ),
            3 to listOf(
                "Jollibee",
                "McDonald's",
                "Savemore",
                "Credit Corp"
            ),
            4 to emptyList(),
            5 to listOf("Alfamart"),
            6 to emptyList(),
            7 to emptyList(),
            8 to listOf(
                "Ay Pendong",
                "Zee Samgyupsal"
            ),
            9 to listOf(
                "Landbank",
                "Skin Deep Aesthetic",
                "BDO",
                "Coffee Amara",
                "RDS Jewelers",
                "Rural Bank of Lipa City Inc.",
                "BPI",
                "Watsons Pharmacy",
                "Dunkin'",
                "EastWest Bank"
            ),
            10 to listOf(
                "Nasugbu Public Market",
                "Newstar",
                "Saveway"
            ),
            11 to listOf(
                "WalterMart",
                "Guangdong",
                "Dunkin'",
                "Immanuel"
            ),
            12 to listOf(
                "Metrobank",
                "Alfamart"
            )
        )

        val barangayPicker = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                (1..12).map { "Barangay $it" }
            )
            setSelection((poblacion - 1).coerceIn(0, 11))
        }

        val pickerParams = LinearLayout.LayoutParams(-1, dp(48))
        pickerParams.topMargin = dp(12)
        content.addView(barangayPicker, pickerParams)

        val statusText = text("", 12f, yellow, true)

        val statusParams = LinearLayout.LayoutParams(-1, -2)
        statusParams.topMargin = dp(8)
        content.addView(statusText, statusParams)

        val locationPicker = Spinner(this)

        val locationParams = LinearLayout.LayoutParams(-1, dp(48))
        locationParams.topMargin = dp(8)
        content.addView(locationPicker, locationParams)

        val mapWebView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            webViewClient = WebViewClient()
            setBackgroundColor(Color.rgb(7, 19, 42))
        }

        val mapParams = LinearLayout.LayoutParams(-1, dp(350))
        mapParams.topMargin = dp(12)
        content.addView(mapWebView, mapParams)

        var selectedLocation = ""
        var currentBarangay = poblacion

        fun loadSelectedMap(name: String, barangay: Int) {
            val query = "$name, Nasugbu, Batangas, Philippines"

            val url =
                "https://www.google.com/maps?q=${Uri.encode(query)}&output=embed"

            mapWebView.loadUrl(url)
        }

        fun showMapFor(name: String, barangay: Int) {
            loadSelectedMap(name, barangay)
        }

        fun showSelectedLocation() {
            val options =
                locationsByBarangay[currentBarangay].orEmpty()

            if (options.isEmpty()) {
                selectedLocation = ""

                statusText.text =
                    "NO AVAILABLE PARKING LOCATION IN BARANGAY $currentBarangay"

                locationPicker.isEnabled = false

                mapWebView.loadDataWithBaseURL(
                    null,
                    """
                    <html>
                    <body style="background:#07132a;color:white;
                    font-family:sans-serif;text-align:center;padding:30px">
                    <h3>No available location</h3>
                    <p>Barangay $currentBarangay has no listed parking location.</p>
                    </body>
                    </html>
                    """.trimIndent(),
                    "text/html",
                    "UTF-8",
                    null
                )

                return
            }

            locationPicker.isEnabled = true

            statusText.text =
                "${options.size} listed location(s) in Barangay $currentBarangay"

            locationPicker.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                options
            )

            locationPicker.setSelection(0)
            selectedLocation = options.first()

            showMapFor(selectedLocation, currentBarangay)
        }

        fun updateSelection(name: String) {
            selectedLocation = name
            showMapFor(name, currentBarangay)
        }

        locationPicker.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val options =
                        locationsByBarangay[currentBarangay].orEmpty()

                    if (position in options.indices) {
                        updateSelection(options[position])
                    }
                }
            }

        barangayPicker.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    currentBarangay = position + 1
                    poblacion = currentBarangay
                    showSelectedLocation()
                }
            }

        button("GET DIRECTIONS TO SELECTED LOCATION", {
            if (selectedLocation.isBlank()) {
                toast(
                    "No parking location is listed for Barangay $currentBarangay."
                )
            } else {
                val destination =
                    "$selectedLocation, Nasugbu, Batangas, Philippines"

                val directionsUrl =
                    "https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(destination)}&travelmode=driving"

                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(directionsUrl)
                        )
                    )
                } catch (e: Exception) {
                    toast("Unable to open Google Maps.")
                }
            }
        })

        button("OPEN SELECTED LOCATION IN GOOGLE MAPS", {
            if (selectedLocation.isNotBlank()) {
                val query =
                    "$selectedLocation, Nasugbu, Batangas, Philippines"

                val url =
                    "https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"

                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                        )
                    )
                } catch (e: Exception) {
                    toast("Unable to open Google Maps.")
                }
            } else {
                toast("No location is listed for this barangay.")
            }
        }, false)

        space(8)

        subtitle(
            "Directions open in Google Maps. Your phone may ask for location permission to show your current position. Confirm parking availability and permission at the establishment before parking."
        )

        showSelectedLocation()
    }

    // SPOT DETAILS

    private fun spotDetails() {
        trackScreen("spotDetails")
        setup()

        title("‹    Spot Details", 20f)

        val photo = FrameLayout(this).apply {
            background = bg(Color.rgb(45, 66, 88))
        }

        val photoText = text("PARKING SPACE #$spot", 16f, white, true)
        photoText.gravity = Gravity.CENTER

        photo.addView(photoText, FrameLayout.LayoutParams(-1, -1))

        val p = LinearLayout.LayoutParams(-1, dp(170))
        p.topMargin = dp(15)
        content.addView(photo, p)

        title("Space #${spot.toString().padStart(2, '0')}", 20f)
        subtitle("Barangay Poblacion $poblacion")
        subtitle("● AVAILABLE")

        space(20)

        val rates = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        left.addView(text("Hourly Rate", 11f, muted))
        left.addView(text("₱30.00", 20f, cyan, true))

        val right = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.RIGHT
        }

        right.addView(text("Daily Pass", 11f, muted))
        right.addView(text("₱240.00", 20f, cyan, true))

        rates.addView(left, LinearLayout.LayoutParams(0, dp(55), 1f))
        rates.addView(right, LinearLayout.LayoutParams(0, dp(55), 1f))

        content.addView(rates)

        title("Managed By", 13f)

        val manager = card()
        cardText(manager, "👤 Captain Juan Dela Cruz", 13f, white, true)
        cardText(manager, "Barangay Desk Officer", 10f, muted)

        button("RESERVE NOW", { reserve() })
    }

    // RESERVATION SCREEN

    private fun reserve() {
        trackScreen("reserve")
        setup(false)

        title("‹    Reserve a Spot", 23f)
        subtitle("Space #$spot • ₱30.00 per hour")

        title("Select reservation date", 13f)

        val date = input("Select Date")
        date.isFocusable = false

        date.setOnClickListener {
            val c = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    date.setText(
                        String.format(
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                        )
                    )
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        title("Start time", 13f)
        val time = input("Example: 09:00 AM")

        title("Duration (hours)", 13f)
        val hours = input("Enter number of hours")
        hours.inputType = 2

        title("Hourly Rate: ₱30.00", 16f)

        button("CONFIRM RESERVATION", {
            val duration = hours.text.toString().toIntOrNull()

            if (
                date.text.isBlank() ||
                time.text.isBlank() ||
                duration == null ||
                duration <= 0
            ) {
                toast("Complete all reservation details")
            } else {
                bookings.add(
                    Booking(
                        spot,
                        poblacion,
                        date.text.toString(),
                        time.text.toString(),
                        duration,
                        "ACTIVE"
                    )
                )

                confirmation()
            }
        })

        button("CANCEL", { goBack() }, false)
    }

    // CONFIRMATION SCREEN

    private fun confirmation() {
        trackScreen("confirmation")
        setup(false)
        space(80)

        val check = text("✓", 45f, green, true)
        check.gravity = Gravity.CENTER

        content.addView(check, LinearLayout.LayoutParams(-1, dp(75)))

        val heading = text("Reservation Confirmed!", 22f, white, true)
        heading.gravity = Gravity.CENTER
        content.addView(heading)

        val msg = text(
            "Your parking spot has been added to My Bookings.",
            12f,
            muted
        )
        msg.gravity = Gravity.CENTER
        content.addView(msg)

        space(30)

        title("Spot #$spot", 18f)
        subtitle("Poblacion $poblacion • Nasugbu, Batangas")

        button("VIEW MY BOOKINGS", { bookings(false) })
        button("BACK TO HOME", { home() }, false)
    }

    // MY BOOKINGS

    private fun bookings(history: Boolean) {
        trackScreen("bookings")
        setup()

        title("My Bookings", 23f)

        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        fun tab(
            label: String,
            selected: Boolean,
            action: () -> Unit
        ) {
            val t = text(label, 12f, white, true)
            t.gravity = Gravity.CENTER
            t.background = bg(if (selected) blue else panel)

            val p = LinearLayout.LayoutParams(0, dp(40), 1f)
            p.setMargins(dp(3), dp(10), dp(3), dp(10))

            tabs.addView(t, p)
            t.setOnClickListener { action() }
        }

        tab("Active", !history) { bookings(false) }
        tab("History", history) { bookings(true) }

        content.addView(tabs)

        val filtered = this.bookings.filter {
            if (history) it.status != "ACTIVE"
            else it.status == "ACTIVE"
        }

        if (filtered.isEmpty()) {
            val empty = text("No bookings found.", 13f, muted)
            empty.gravity = Gravity.CENTER
            content.addView(empty)
        }

        filtered.reversed().forEach { booking ->
            val c = card()

            cardText(
                c,
                "Spot #${booking.spot.toString().padStart(2, '0')}     ${booking.status}",
                12f,
                if (booking.status == "ACTIVE") green else muted,
                true
            )

            cardText(c, "Poblacion ${booking.barangay}", 10f, muted)

            cardText(
                c,
                "Date & Time: ${booking.date} - ${booking.time}",
                10f,
                muted
            )

            cardText(
                c,
                "Duration: ${booking.hours} hrs",
                10f,
                muted
            )

            cardText(
                c,
                "Total Cost: ₱${booking.hours * 30}.00",
                12f,
                cyan,
                true
            )

            if (booking.status == "ACTIVE") {
                val cancel = Button(this).apply {
                    text = "CANCEL BOOKING"
                    textSize = 10f
                    isAllCaps = false
                    setTextColor(white)
                    background = bg(Color.rgb(35, 48, 75))

                    setOnClickListener {
                        booking.status = "CANCELLED"
                        bookings(false)
                        toast("Booking cancelled")
                    }
                }

                c.addView(cancel, LinearLayout.LayoutParams(-1, dp(42)))
            }
        }
    }

    // PROFILE SCREEN

    private fun profile() {
        trackScreen("profile")
        setup()

        title("My Profile                         ✎", 22f)
        space(20)

        val avatar = text("AB", 27f, cyan, true)
        avatar.gravity = Gravity.CENTER
        avatar.background = bg(panel, 50)

        content.addView(
            avatar,
            LinearLayout.LayoutParams(dp(78), dp(78)).apply {
                gravity = Gravity.CENTER
            }
        )

        val name = text("Andres Bonifacio", 16f, white, true)
        name.gravity = Gravity.CENTER
        content.addView(name)

        val email = text("andres.bonifacio@email.com", 10f, muted)
        email.gravity = Gravity.CENTER
        content.addView(email)

        val phone = text("+63 917 123 4567", 10f, muted)
        phone.gravity = Gravity.CENTER
        content.addView(phone)

        space(18)

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        fun stat(number: String, description: String) {
            val c = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                background = bg(panel)
                setPadding(dp(8), dp(12), dp(8), dp(12))
            }

            c.addView(text(number, 22f, cyan, true))
            c.addView(text(description, 10f, muted))

            stats.addView(c, LinearLayout.LayoutParams(0, dp(75), 1f))
        }

        stat("42", "Total Parkings")
        stats.addView(View(this), LinearLayout.LayoutParams(dp(8), 1))
        stat("28 hrs", "Saved Time")

        content.addView(stats)

        fun option(
            icon: String,
            name: String,
            message: String
        ) {
            val c = card()

            val t = text(
                "$icon     $name                         ›",
                12f,
                white,
                true
            )

            c.addView(t)
            c.setOnClickListener { toast(message) }
        }

        option("▱", "My Vehicle", "Honda Civic • NBO 1234")
        option("▣", "Payment Methods", "GCash linked")
        option("☷", "Barangay Settings", "Poblacion 1 to 12")
        option("ⓘ", "Help & Support", "Contact ParkMate support")

        button("⇥  SIGN OUT", {
            screenHistory.clear()
            currentScreen = null
            welcome()
        }, false)
    }
}