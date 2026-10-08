const api = "";
const state = {
    user: JSON.parse(localStorage.getItem("parkshareUser") || "null"),
    spaces: [],
    bookings: [],
    lastBooking: null
};

const views = {
    home: document.getElementById("homeView"),
    login: document.getElementById("authView"),
    register: document.getElementById("authView"),
    search: document.getElementById("searchView"),
    dashboard: document.getElementById("dashboardView"),
    bookings: document.getElementById("bookingsView")
};

function showToast(message, isError = false) {
    const toast = document.getElementById("toast");
    toast.textContent = message;
    toast.style.background = isError ? "#c2413b" : "#0f2133";
    toast.classList.remove("hidden");
    setTimeout(() => toast.classList.add("hidden"), 3200);
}

function setStatus(id, message, type = "") {
    const element = document.getElementById(id);
    if (!element) return;
    element.textContent = message;
    element.className = `inline-status ${type}`.trim();
    element.classList.toggle("hidden", !message);
}

function skeletonCards(count = 3) {
    return `<div class="skeleton-grid">${Array.from({ length: count })
        .map(() => `<div class="skeleton-card"></div>`)
        .join("")}</div>`;
}

function emptyState(title, message, action = "") {
    return `
        <div class="empty-state">
            <h3>${title}</h3>
            <p>${message}</p>
            ${action}
        </div>
    `;
}

function setButtonLoading(button, loadingText) {
    if (!button) return () => {};
    const originalText = button.textContent;
    button.disabled = true;
    button.textContent = loadingText;
    return () => {
        button.disabled = false;
        button.textContent = originalText;
    };
}

async function request(path, options = {}) {
    const response = await fetch(api + path, {
        headers: { "Content-Type": "application/json" },
        ...options
    });
    if (!response.ok) {
        const error = await response.json().catch(() => ({ message: "Something went wrong." }));
        throw new Error(error.message || "Something went wrong.");
    }
    if (response.status === 204) {
        return null;
    }
    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

function setRoute(route) {
    Object.values(views).forEach(view => view.classList.add("hidden"));
    const selected = views[route] || views.home;
    selected.classList.remove("hidden");

    if (route === "register") {
        document.getElementById("registerForm").scrollIntoView({ behavior: "smooth", block: "center" });
    }
    if (route === "search") {
        loadParkingSpaces().catch(error => showToast(error.message, true));
    }
    if (route === "dashboard") {
        requireLogin();
        loadDashboard().catch(error => showToast(error.message, true));
    }
    if (route === "bookings") {
        requireLogin();
        loadBookingsView().catch(error => showToast(error.message, true));
    }
}

function requireLogin() {
    if (!state.user) {
        setRoute("login");
        showToast("Please login first.", true);
        throw new Error("Login required");
    }
}

function updateHeader() {
    const isLoggedIn = Boolean(state.user);
    document.getElementById("currentUser").textContent = isLoggedIn
        ? `${state.user.name} (${state.user.role})`
        : "";
    document.getElementById("loginOpenBtn").classList.toggle("hidden", isLoggedIn);
    document.getElementById("registerOpenBtn").classList.toggle("hidden", isLoggedIn);
    document.getElementById("logoutBtn").classList.toggle("hidden", !isLoggedIn);
    document.querySelectorAll(".auth-link").forEach(link => link.classList.toggle("hidden", !isLoggedIn));
}

function parkingCard(space) {
    return `
        <article class="parking-card">
            <div class="parking-image"></div>
            <div class="card-top">
                <h3>${space.title}</h3>
                <span class="badge">${formatVehicle(space.vehicleType)}</span>
            </div>
            <p>${space.address}</p>
            <div class="card-top">
                <span class="price">Rs ${space.pricePerHour}/hr</span>
                <span class="badge warn">${space.host?.name || "Host"}</span>
            </div>
            <p class="hint">Saved in MySQL through Spring Data JPA and ready for live booking.</p>
            <button class="btn primary" onclick="openDetails(${space.id})">View details</button>
        </article>
    `;
}

function formatVehicle(type) {
    return (type || "").replace("_", " ").toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
}

async function loadParkingSpaces() {
    setStatus("searchStatus", "Loading parking spaces...");
    setStatus("featuredStatus", "Loading featured spaces...");
    document.getElementById("parkingGrid").innerHTML = skeletonCards(6);
    document.getElementById("featuredGrid").innerHTML = skeletonCards(3);

    const location = document.getElementById("filterLocation").value.trim();
    const vehicleType = document.getElementById("filterVehicle").value;
    const maxPrice = document.getElementById("filterPrice").value;
    const params = new URLSearchParams();
    if (location) params.set("location", location);
    if (vehicleType) params.set("vehicleType", vehicleType);
    if (maxPrice) params.set("maxPrice", maxPrice);

    try {
        state.spaces = await request(`/api/parking${params.toString() ? `?${params}` : ""}`);
    } catch (error) {
        setStatus("searchStatus", "Could not load parking spaces. Check that the Spring Boot app and MySQL are running.", "error");
        setStatus("featuredStatus", "Could not load featured spaces.", "error");
        document.getElementById("parkingGrid").innerHTML = emptyState(
            "Backend not reachable",
            "Start the application with mvn spring-boot:run, then refresh this page."
        );
        document.getElementById("featuredGrid").innerHTML = "";
        throw error;
    }

    const html = state.spaces.length
        ? state.spaces.map(parkingCard).join("")
        : emptyState("No spaces found", "Try a different location, vehicle type, or price.");
    document.getElementById("parkingGrid").innerHTML = html;
    document.getElementById("featuredGrid").innerHTML = state.spaces.length
        ? state.spaces.slice(0, 3).map(parkingCard).join("")
        : emptyState("No featured spaces yet", "Add a host parking space to show it here.");
    document.getElementById("homeSpacesCount").textContent = state.spaces.length;
    setStatus("searchStatus", `${state.spaces.length} parking space${state.spaces.length === 1 ? "" : "s"} available`, "success");
    setStatus("featuredStatus", "Live spaces loaded from the backend", "success");
}

async function openDetails(id) {
    const [space, availability, reviews] = await Promise.all([
        request(`/api/parking/${id}`),
        request(`/api/parking/${id}/availability`),
        request(`/api/reviews/parking/${id}`)
    ]);

    const dateValue = document.getElementById("filterDate").value || "2026-10-10";
    const startValue = document.getElementById("filterStart").value || "10:00";
    const endValue = document.getElementById("filterEnd").value || "12:00";
    const availabilityText = availability.length
        ? availability.map(item => `${item.date}: ${item.startTime} - ${item.endTime}`).join("<br>")
        : "Host has not added availability.";
    const reviewText = reviews.length
        ? reviews.map(review => `<p><strong>${review.rating}/5</strong> ${review.comment || ""}</p>`).join("")
        : "<p>No reviews yet.</p>";

    document.getElementById("detailContent").innerHTML = `
        <div class="detail-grid">
            <div>
                <div class="detail-hero"></div>
                <span class="eyebrow">Parking details</span>
                <h1>${space.title}</h1>
                <p>${space.address}</p>
                <div class="detail-meta">
                    <div><span class="hint">Vehicle</span><strong>${formatVehicle(space.vehicleType)}</strong></div>
                    <div><span class="hint">Price</span><strong>Rs ${space.pricePerHour}/hr</strong></div>
                    <div><span class="hint">Host</span><strong>${space.host?.name || "Host"}</strong></div>
                </div>
                <p><strong>Host email:</strong> ${space.host?.email || "Not available"}</p>
                <p><strong>Available timings:</strong><br>${availabilityText}</p>
                <h3>Reviews</h3>
                ${reviewText}
            </div>
            <form id="bookingForm" class="booking-box">
                <h2>Book this space</h2>
                <div class="booking-summary">
                    <span>Choose date and time. The backend will check conflicts before saving.</span>
                    <span>Estimated from selected filters: ${dateValue}, ${startValue} to ${endValue}</span>
                </div>
                <label>Date
                    <input name="bookingDate" type="date" value="${dateValue}" required>
                </label>
                <label>Start time
                    <input name="startTime" type="time" value="${startValue}" required>
                </label>
                <label>End time
                    <input name="endTime" type="time" value="${endValue}" required>
                </label>
                <button class="btn primary">Confirm booking</button>
                <p class="hint">The backend checks availability and calculates price before saving.</p>
            </form>
        </div>
    `;
    document.getElementById("bookingForm").addEventListener("submit", event => bookSpace(event, space.id));
    document.getElementById("detailModal").classList.remove("hidden");
}

async function bookSpace(event, parkingSpaceId) {
    event.preventDefault();
    if (!state.user || state.user.role !== "DRIVER") {
        showToast("Please login as a driver to book parking.", true);
        return;
    }
    const stopLoading = setButtonLoading(event.submitter, "Booking...");
    const form = new FormData(event.target);
    try {
        const booking = await request("/api/bookings", {
            method: "POST",
            body: JSON.stringify({
                parkingSpaceId,
                driverId: state.user.id,
                bookingDate: form.get("bookingDate"),
                startTime: form.get("startTime"),
                endTime: form.get("endTime")
            })
        });
        await request("/api/payments", {
            method: "POST",
            body: JSON.stringify({ bookingId: booking.id, paymentMethod: "SIMULATED_UPI" })
        });
        state.lastBooking = booking;
        document.getElementById("detailContent").innerHTML = bookingConfirmation(booking);
        showToast(`Booking confirmed. Total price: Rs ${booking.totalPrice}`);
    } catch (error) {
        showToast(error.message, true);
    } finally {
        stopLoading();
    }
}

function bookingConfirmation(booking) {
    return `
        <div class="booking-confirmation">
            <span class="eyebrow">Booking confirmed</span>
            <h1>${booking.parkingSpace?.title || "Parking space booked"}</h1>
            <p>Your booking and simulated payment were saved successfully in MySQL.</p>
            <div class="detail-meta">
                <div><span class="hint">Date</span><strong>${booking.bookingDate}</strong></div>
                <div><span class="hint">Time</span><strong>${booking.startTime} - ${booking.endTime}</strong></div>
                <div><span class="hint">Total</span><strong>Rs ${booking.totalPrice}</strong></div>
            </div>
            <div class="hero-actions">
                <button class="btn primary" onclick="document.getElementById('detailModal').classList.add('hidden'); setRoute('bookings')">View my bookings</button>
                <button class="btn ghost" onclick="document.getElementById('detailModal').classList.add('hidden'); setRoute('search')">Search more</button>
            </div>
        </div>
    `;
}

async function loadDashboard() {
    const isHost = state.user.role === "HOST";
    document.getElementById("dashboardAvatar").textContent = state.user.name.charAt(0).toUpperCase();
    document.getElementById("sidebarName").textContent = state.user.name;
    document.getElementById("sidebarRole").textContent = `${state.user.role} dashboard`;
    document.querySelectorAll(".host-only").forEach(el => el.classList.toggle("hidden", !isHost));
    document.getElementById("dashboardTitle").textContent = isHost ? "Host overview" : "Driver overview";
    document.getElementById("dashboardSubtitle").textContent = isHost
        ? "Manage listed spaces, booking requests, and earnings for the first evaluation demo."
        : "Search parking, track bookings, cancel when needed, and submit reviews.";
    if (isHost) {
        setStatus("hostSpacesStatus", "Loading your parking spaces...");
        setStatus("dashboardBookingsStatus", "Loading host bookings...");
        const spaces = await request(`/api/parking/host/${state.user.id}`);
        const bookings = await request(`/api/bookings/host/${state.user.id}`);
        state.bookings = bookings;
        const earnings = bookings
            .filter(booking => booking.status !== "CANCELLED")
            .reduce((sum, booking) => sum + Number(booking.totalPrice || 0), 0);
        document.getElementById("statsGrid").innerHTML = statCards([
            ["Total listings", spaces.length],
            ["Total bookings", bookings.length],
            ["Estimated earnings", `Rs ${earnings.toFixed(2)}`]
        ]);
        document.getElementById("dashboardQuickPanel").innerHTML = `
            <article>
                <h3>First demo flow</h3>
                <p>Use Add space to create a new parking record, then search for it as a driver.</p>
                <button class="btn secondary" data-panel-target="addSpacePanel">Add parking space</button>
            </article>
            <article>
                <h3>Database proof</h3>
                <p>After saving, check the parking_space and availability tables in MySQL.</p>
            </article>
        `;
        document.getElementById("hostSpacesGrid").innerHTML = spaces.length
            ? spaces.map(hostSpaceCard).join("")
            : emptyState("No listings yet", "Add a parking space to start receiving bookings.", `<button class="btn secondary" data-panel-target="addSpacePanel">Add first space</button>`);
        setStatus("hostSpacesStatus", `${spaces.length} listing${spaces.length === 1 ? "" : "s"} loaded`, "success");
        renderBookingsTable("dashboardBookings", bookings, false);
        setStatus("dashboardBookingsStatus", `${bookings.length} booking${bookings.length === 1 ? "" : "s"} found`, "success");
    } else {
        setStatus("dashboardBookingsStatus", "Loading driver bookings...");
        const bookings = await request(`/api/bookings/driver/${state.user.id}`);
        state.bookings = bookings;
        document.getElementById("statsGrid").innerHTML = statCards([
            ["Total bookings", bookings.length],
            ["Active bookings", bookings.filter(b => b.status === "CONFIRMED").length],
            ["Cancelled", bookings.filter(b => b.status === "CANCELLED").length]
        ]);
        document.getElementById("dashboardQuickPanel").innerHTML = `
            <article>
                <h3>Book a space</h3>
                <p>Search available parking, open details, and complete the booking flow.</p>
                <button class="btn primary" data-route="search">Search parking</button>
            </article>
            <article>
                <h3>Database proof</h3>
                <p>After booking, check the booking and payment tables in MySQL.</p>
            </article>
        `;
        renderBookingsTable("dashboardBookings", bookings, true);
        setStatus("dashboardBookingsStatus", `${bookings.length} booking${bookings.length === 1 ? "" : "s"} found`, "success");
    }
}

function statCards(items) {
    return items.map(([label, value]) => `
        <article class="stat-card">
            <span class="eyebrow">${label}</span>
            <h2>${value}</h2>
        </article>
    `).join("");
}

function hostSpaceCard(space) {
    return `
        <article class="parking-card">
            <div class="card-top">
                <h3>${space.title}</h3>
                <span class="badge">${formatVehicle(space.vehicleType)}</span>
            </div>
            <p>${space.address}</p>
            <span class="price">Rs ${space.pricePerHour}/hr</span>
            <p class="hint">Visible to drivers in search while active.</p>
            <div class="card-top">
                <button class="btn ghost" onclick='editSpace(${JSON.stringify(space)})'>Edit</button>
                <button class="btn danger" onclick="deleteSpace(${space.id})">Delete</button>
            </div>
        </article>
    `;
}

function editSpace(space) {
    showPanel("addSpacePanel");
    document.getElementById("spaceFormTitle").textContent = "Edit parking space";
    const form = document.getElementById("spaceForm");
    form.elements.id.value = space.id;
    form.elements.title.value = space.title;
    form.elements.address.value = space.address;
    form.elements.pricePerHour.value = space.pricePerHour;
    form.elements.vehicleType.value = space.vehicleType;
    form.elements.latitude.value = space.latitude || "";
    form.elements.longitude.value = space.longitude || "";
}

async function deleteSpace(id) {
    if (!confirm("Delete this parking space from active listings?")) return;
    try {
        await request(`/api/parking/${id}?hostId=${state.user.id}`, { method: "DELETE" });
        showToast("Parking space deleted.");
        await loadDashboard();
        await loadParkingSpaces();
    } catch (error) {
        showToast(error.message, true);
    }
}

async function loadBookingsView() {
    setStatus("myBookingsStatus", "Loading bookings...");
    if (state.user.role === "HOST") {
        state.bookings = await request(`/api/bookings/host/${state.user.id}`);
        renderBookingsTable("myBookings", state.bookings, false);
    } else {
        state.bookings = await request(`/api/bookings/driver/${state.user.id}`);
        renderBookingsTable("myBookings", state.bookings, true);
    }
    setStatus("myBookingsStatus", `${state.bookings.length} booking${state.bookings.length === 1 ? "" : "s"} loaded`, "success");
}

function renderBookingsTable(containerId, bookings, driverActions) {
    const container = document.getElementById(containerId);
    if (!bookings.length) {
        container.innerHTML = emptyState("No bookings yet", "Your booking activity will appear here after a driver books a space.");
        return;
    }
    container.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>Space</th>
                    <th>Date and time</th>
                    <th>Driver</th>
                    <th>Total</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                ${bookings.map(booking => `
                    <tr>
                        <td>${booking.parkingSpace?.title || "Parking space"}<br><span class="hint">${booking.parkingSpace?.address || ""}</span></td>
                        <td>${booking.bookingDate}<br>${booking.startTime} - ${booking.endTime}</td>
                        <td>${booking.driver?.name || ""}</td>
                        <td>Rs ${booking.totalPrice}</td>
                        <td><span class="badge ${booking.status === "CANCELLED" ? "danger" : ""}">${booking.status}</span></td>
                        <td>${bookingActions(booking, driverActions)}</td>
                    </tr>
                `).join("")}
            </tbody>
        </table>
    `;
}

function bookingActions(booking, driverActions) {
    if (!driverActions) {
        return `<button class="btn ghost" onclick="completeBooking(${booking.id})">Mark completed</button>`;
    }
    const cancel = booking.status === "CONFIRMED"
        ? `<button class="btn danger" onclick="cancelBooking(${booking.id})">Cancel</button>`
        : "";
    const review = booking.status === "CONFIRMED" || booking.status === "COMPLETED"
        ? `<button class="btn ghost" onclick="reviewBooking(${booking.id}, ${booking.parkingSpace?.id})">Review</button>`
        : "";
    return `${cancel} ${review}`;
}

async function cancelBooking(id) {
    try {
        await request(`/api/bookings/${id}/cancel?driverId=${state.user.id}`, { method: "PUT" });
        showToast("Booking cancelled. The space is available again.");
        await loadBookingsView();
        await loadDashboard();
    } catch (error) {
        showToast(error.message, true);
    }
}

async function completeBooking(id) {
    try {
        await request(`/api/bookings/${id}/complete`, { method: "PUT" });
        showToast("Booking marked completed.");
        await loadDashboard();
    } catch (error) {
        showToast(error.message, true);
    }
}

async function reviewBooking(bookingId, parkingSpaceId) {
    const rating = prompt("Rating from 1 to 5", "5");
    if (!rating) return;
    const comment = prompt("Short review", "Good parking experience.");
    try {
        await request("/api/reviews", {
            method: "POST",
            body: JSON.stringify({
                bookingId,
                parkingSpaceId,
                driverId: state.user.id,
                rating: Number(rating),
                comment
            })
        });
        showToast("Review submitted.");
    } catch (error) {
        showToast(error.message, true);
    }
}

function showPanel(panelId) {
    document.querySelectorAll(".dash-panel").forEach(panel => panel.classList.add("hidden"));
    document.getElementById(panelId).classList.remove("hidden");
    document.querySelectorAll(".side-link").forEach(button => {
        button.classList.toggle("active", button.dataset.panel === panelId);
    });
}

document.addEventListener("click", event => {
    const route = event.target.dataset.route;
    if (route) {
        event.preventDefault();
        setRoute(route);
    }
    const panel = event.target.dataset.panel || event.target.dataset.panelTarget;
    if (panel) {
        showPanel(panel);
    }
});

document.getElementById("filterBtn").addEventListener("click", loadParkingSpaces);
document.getElementById("clearFiltersBtn").addEventListener("click", () => {
    document.getElementById("filterLocation").value = "";
    document.getElementById("filterVehicle").value = "";
    document.getElementById("filterPrice").value = "";
    loadParkingSpaces().catch(error => showToast(error.message, true));
});
document.getElementById("heroSearchBtn").addEventListener("click", () => {
    document.getElementById("filterLocation").value = document.getElementById("heroLocation").value;
    document.getElementById("filterVehicle").value = document.getElementById("heroVehicle").value;
    setRoute("search");
});

document.getElementById("closeModalBtn").addEventListener("click", () => {
    document.getElementById("detailModal").classList.add("hidden");
});

document.getElementById("logoutBtn").addEventListener("click", () => {
    localStorage.removeItem("parkshareUser");
    state.user = null;
    updateHeader();
    setRoute("home");
});

document.getElementById("loginForm").addEventListener("submit", async event => {
    event.preventDefault();
    const stopLoading = setButtonLoading(event.submitter, "Logging in...");
    const form = new FormData(event.target);
    try {
        state.user = await request("/api/users/login", {
            method: "POST",
            body: JSON.stringify(Object.fromEntries(form.entries()))
        });
        localStorage.setItem("parkshareUser", JSON.stringify(state.user));
        updateHeader();
        showToast("Login successful.");
        setRoute("dashboard");
    } catch (error) {
        showToast(error.message, true);
    } finally {
        stopLoading();
    }
});

document.getElementById("registerForm").addEventListener("submit", async event => {
    event.preventDefault();
    const stopLoading = setButtonLoading(event.submitter, "Creating account...");
    const form = new FormData(event.target);
    try {
        state.user = await request("/api/users/register", {
            method: "POST",
            body: JSON.stringify(Object.fromEntries(form.entries()))
        });
        localStorage.setItem("parkshareUser", JSON.stringify(state.user));
        updateHeader();
        showToast("Registration successful.");
        setRoute("dashboard");
    } catch (error) {
        showToast(error.message, true);
    } finally {
        stopLoading();
    }
});

document.getElementById("spaceForm").addEventListener("submit", async event => {
    event.preventDefault();
    const stopLoading = setButtonLoading(event.submitter, "Saving...");
    const form = event.target;
    const data = Object.fromEntries(new FormData(form).entries());
    const id = data.id;
    delete data.id;
    data.hostId = state.user.id;
    data.pricePerHour = Number(data.pricePerHour);
    data.latitude = data.latitude ? Number(data.latitude) : null;
    data.longitude = data.longitude ? Number(data.longitude) : null;
    try {
        await request(id ? `/api/parking/${id}` : "/api/parking", {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(data)
        });
        form.reset();
        form.elements.id.value = "";
        document.getElementById("spaceFormTitle").textContent = "Add parking space";
        showToast("Parking space saved.");
        showPanel("hostSpacesPanel");
        loadDashboard();
        loadParkingSpaces();
    } catch (error) {
        showToast(error.message, true);
    } finally {
        stopLoading();
    }
});

window.openDetails = openDetails;
window.editSpace = editSpace;
window.deleteSpace = deleteSpace;
window.cancelBooking = cancelBooking;
window.completeBooking = completeBooking;
window.reviewBooking = reviewBooking;

updateHeader();
loadParkingSpaces().catch(error => showToast(error.message, true));
setRoute("home");
