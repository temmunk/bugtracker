const API = '/api/bugs';
const AUTH_API = '/api/auth';

let currentPage = 0;
let currentSort = 'createdAt';
let currentSortDir = 'desc';
let totalPages = 0;

const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => document.querySelectorAll(sel);

// Elements
const bugTableBody = $('#bugTableBody');
const bugForm = $('#bugForm');
const bugFormElement = $('#bugFormElement');
const formTitle = $('#formTitle');
const emptyState = $('#emptyState');
const bugTable = $('#bugTable');
const pagination = $('#pagination');
const searchInput = $('#searchInput');
const statusFilter = $('#statusFilter');
const priorityFilter = $('#priorityFilter');

// ---------- AUTH ----------

function getToken() {
    return localStorage.getItem('jwt_token');
}

function getUser() {
    const raw = localStorage.getItem('jwt_user');
    return raw ? JSON.parse(raw) : null;
}

function setAuth(token, username, role) {
    localStorage.setItem('jwt_token', token);
    localStorage.setItem('jwt_user', JSON.stringify({ username, role }));
}

function clearAuth() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('jwt_user');
}

function isLoggedIn() {
    return !!getToken();
}

function authFetch(url, options = {}) {
    const token = getToken();
    if (token) {
        options.headers = {
            ...options.headers,
            'Authorization': `Bearer ${token}`
        };
    }
    return fetch(url, options);
}

function updateAuthUI() {
    const loggedIn = isLoggedIn();
    const user = getUser();

    const authView = $('#authView');
    const userInfo = $('#userInfo');

    if (loggedIn && user) {
        authView.classList.add('hidden');
        userInfo.classList.remove('hidden');
        $('#usernameDisplay').textContent = user.username;
        $$('.auth-required').forEach(el => el.classList.remove('hidden'));
    } else {
        authView.classList.remove('hidden');
        userInfo.classList.add('hidden');
        $$('.auth-required').forEach(el => el.classList.add('hidden'));
    }
}

function showAppViews() {
    if (!isLoggedIn()) {
        $$('.view').forEach(v => v.classList.add('hidden'));
        $('#authView').classList.remove('hidden');
        return;
    }
    $('#authView').classList.add('hidden');
}

// Auth tabs
$$('.auth-tab').forEach(tab => {
    tab.addEventListener('click', () => {
        $$('.auth-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        if (tab.dataset.tab === 'login') {
            $('#loginForm').classList.remove('hidden');
            $('#registerForm').classList.add('hidden');
        } else {
            $('#loginForm').classList.add('hidden');
            $('#registerForm').classList.remove('hidden');
        }
        $('#loginError').classList.add('hidden');
        $('#registerError').classList.add('hidden');
        $('#registerSuccess').classList.add('hidden');
    });
});

// Login
$('#loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const errorEl = $('#loginError');
    errorEl.classList.add('hidden');

    const payload = {
        username: $('#loginUsername').value,
        password: $('#loginPassword').value
    };

    try {
        const res = await fetch(`${AUTH_API}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const data = await res.json();
            errorEl.textContent = data.message || 'Login failed';
            errorEl.classList.remove('hidden');
            return;
        }

        const data = await res.json();
        setAuth(data.token, data.username, data.role);
        $('#loginForm').reset();
        updateAuthUI();
        showAppViews();
        $('#bugsView').classList.remove('hidden');
        $$('.nav-btn').forEach(b => b.classList.remove('active'));
        $$('.nav-btn')[0].classList.add('active');
        loadBugs();
    } catch (err) {
        errorEl.textContent = 'Connection error. Please try again.';
        errorEl.classList.remove('hidden');
    }
});

// Register
$('#registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const errorEl = $('#registerError');
    const successEl = $('#registerSuccess');
    errorEl.classList.add('hidden');
    successEl.classList.add('hidden');

    const payload = {
        username: $('#regUsername').value,
        password: $('#regPassword').value,
        email: $('#regEmail').value || null
    };

    try {
        const res = await fetch(`${AUTH_API}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (!res.ok) {
            errorEl.textContent = data.message || data.errors?.username || 'Registration failed';
            errorEl.classList.remove('hidden');
            return;
        }

        successEl.textContent = 'Account created! You can now log in.';
        successEl.classList.remove('hidden');
        $('#registerForm').reset();
    } catch (err) {
        errorEl.textContent = 'Connection error. Please try again.';
        errorEl.classList.remove('hidden');
    }
});

// Logout
$('#logoutBtn').addEventListener('click', () => {
    clearAuth();
    updateAuthUI();
    showAppViews();
});

// Nav
$$('.nav-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        if (!isLoggedIn()) return;
        $$('.nav-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        $$('.view').forEach(v => v.classList.add('hidden'));
        const view = btn.dataset.view;
        $(`#${view}View`).classList.remove('hidden');
        if (view === 'dashboard') loadDashboard();
        if (view === 'activity') loadGlobalActivity();
    });
});

// Bug list events
$('#newBugBtn').addEventListener('click', () => openForm());
$('#cancelBtn').addEventListener('click', () => closeForm());
bugFormElement.addEventListener('submit', handleSubmit);
$('#searchBtn').addEventListener('click', () => { currentPage = 0; loadBugs(); });
searchInput.addEventListener('keydown', (e) => { if (e.key === 'Enter') { currentPage = 0; loadBugs(); } });
statusFilter.addEventListener('change', () => { priorityFilter.value = ''; currentPage = 0; loadBugs(); });
priorityFilter.addEventListener('change', () => { statusFilter.value = ''; currentPage = 0; loadBugs(); });

// Sorting
$$('th.sortable').forEach(th => {
    th.addEventListener('click', () => {
        const col = th.dataset.sort;
        if (currentSort === col) {
            currentSortDir = currentSortDir === 'asc' ? 'desc' : 'asc';
        } else {
            currentSort = col;
            currentSortDir = 'asc';
        }
        currentPage = 0;
        loadBugs();
    });
});

// Modal
$('#closeModal').addEventListener('click', closeModal);
$('#bugDetailModal').addEventListener('click', (e) => {
    if (e.target.id === 'bugDetailModal') closeModal();
});

// Comment form
$('#commentForm').addEventListener('submit', handleCommentSubmit);

// Init
updateAuthUI();
if (isLoggedIn()) {
    loadBugs();
} else {
    showAppViews();
}

// ---------- BUG LIST ----------

async function loadBugs() {
    try {
        const params = new URLSearchParams({
            page: currentPage,
            size: 10,
            sortBy: currentSort,
            sortDir: currentSortDir
        });

        const search = searchInput.value.trim();
        const status = statusFilter.value;
        const priority = priorityFilter.value;

        if (search) params.set('search', search);
        else if (status) params.set('status', status);
        else if (priority) params.set('priority', priority);

        const res = await authFetch(`${API}?${params}`);
        const data = await res.json();
        totalPages = data.totalPages;
        renderBugs(data.content);
        renderPagination(data);
        updateSortHeaders();
    } catch (err) {
        console.error('Failed to load bugs:', err);
    }
}

function renderBugs(bugs) {
    bugTableBody.innerHTML = '';

    if (bugs.length === 0) {
        bugTable.classList.add('hidden');
        emptyState.classList.remove('hidden');
        pagination.classList.add('hidden');
        return;
    }

    bugTable.classList.remove('hidden');
    emptyState.classList.add('hidden');

    const loggedIn = isLoggedIn();

    bugs.forEach(bug => {
        const row = document.createElement('tr');
        const actionsHtml = loggedIn
            ? `<td class="actions">
                <button class="btn btn-edit" onclick="editBug(${bug.id})">Edit</button>
                <button class="btn btn-danger" onclick="deleteBug(${bug.id})">Delete</button>
               </td>`
            : `<td></td>`;

        row.innerHTML = `
            <td>#${bug.id}</td>
            <td><span class="bug-title" onclick="viewBug(${bug.id})">${escapeHtml(bug.title)}</span></td>
            <td><span class="badge badge-${bug.priority.toLowerCase()}">${bug.priority}</span></td>
            <td><span class="badge badge-${bug.status.toLowerCase()}">${formatStatus(bug.status)}</span></td>
            <td>${escapeHtml(bug.assignee || '\u2014')}</td>
            <td>${formatDate(bug.createdAt)}</td>
            ${actionsHtml}
        `;
        bugTableBody.appendChild(row);
    });
}

function renderPagination(data) {
    if (data.totalPages <= 1) {
        pagination.classList.add('hidden');
        return;
    }

    pagination.classList.remove('hidden');
    pagination.innerHTML = '';

    const prevBtn = document.createElement('button');
    prevBtn.textContent = '\u25C0 Prev';
    prevBtn.disabled = data.first;
    prevBtn.addEventListener('click', () => { currentPage--; loadBugs(); });
    pagination.appendChild(prevBtn);

    const info = document.createElement('span');
    info.className = 'page-info';
    info.textContent = `Page ${data.number + 1} of ${data.totalPages} (${data.totalElements} total)`;
    pagination.appendChild(info);

    const nextBtn = document.createElement('button');
    nextBtn.textContent = 'Next \u25B6';
    nextBtn.disabled = data.last;
    nextBtn.addEventListener('click', () => { currentPage++; loadBugs(); });
    pagination.appendChild(nextBtn);
}

function updateSortHeaders() {
    $$('th.sortable').forEach(th => {
        th.classList.remove('sort-asc', 'sort-desc');
        if (th.dataset.sort === currentSort) {
            th.classList.add(currentSortDir === 'asc' ? 'sort-asc' : 'sort-desc');
        }
    });
}

// ---------- FORM ----------

function openForm(bug = null) {
    if (!isLoggedIn()) return;
    bugForm.classList.remove('hidden');
    if (bug) {
        formTitle.textContent = 'Edit Bug';
        $('#bugId').value = bug.id;
        $('#title').value = bug.title;
        $('#description').value = bug.description || '';
        $('#priority').value = bug.priority;
        $('#status').value = bug.status;
        $('#reporter').value = bug.reporter || '';
        $('#assignee').value = bug.assignee || '';
    } else {
        formTitle.textContent = 'Report a Bug';
        bugFormElement.reset();
        $('#bugId').value = '';
        const user = getUser();
        if (user) $('#reporter').value = user.username;
    }
    $('#title').focus();
}

function closeForm() {
    bugForm.classList.add('hidden');
    bugFormElement.reset();
    $('#bugId').value = '';
}

async function handleSubmit(e) {
    e.preventDefault();
    const bugId = $('#bugId').value;
    const payload = {
        title: $('#title').value,
        description: $('#description').value,
        priority: $('#priority').value,
        status: $('#status').value,
        reporter: $('#reporter').value,
        assignee: $('#assignee').value
    };

    try {
        const url = bugId ? `${API}/${bugId}` : API;
        const method = bugId ? 'PUT' : 'POST';
        const res = await authFetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json();
            alert(err.message || 'Failed to save bug');
            return;
        }
        closeForm();
        loadBugs();
    } catch (err) {
        console.error('Failed to save bug:', err);
    }
}

async function editBug(id) {
    if (!isLoggedIn()) return;
    try {
        const res = await authFetch(`${API}/${id}`);
        const bug = await res.json();
        openForm(bug);
    } catch (err) {
        console.error('Failed to load bug:', err);
    }
}

async function deleteBug(id) {
    if (!isLoggedIn()) return;
    if (!confirm('Are you sure you want to delete this bug?')) return;
    try {
        await authFetch(`${API}/${id}`, { method: 'DELETE' });
        loadBugs();
    } catch (err) {
        console.error('Failed to delete bug:', err);
    }
}

// ---------- BUG DETAIL MODAL ----------

async function viewBug(id) {
    try {
        const [bugRes, commentsRes, activityRes] = await Promise.all([
            authFetch(`${API}/${id}`),
            authFetch(`${API}/${id}/comments`),
            authFetch(`/api/activity/bug/${id}`)
        ]);

        const bug = await bugRes.json();
        const comments = await commentsRes.json();
        const activity = await activityRes.json();

        renderBugDetail(bug, comments, activity);
        $('#bugDetailModal').classList.remove('hidden');
        $('#bugDetailModal').dataset.bugId = id;
    } catch (err) {
        console.error('Failed to load bug detail:', err);
    }
}

function renderBugDetail(bug, comments, activity) {
    const body = $('#bugDetailBody');
    body.innerHTML = `
        <div class="detail-header">
            <h2>#${bug.id} — ${escapeHtml(bug.title)}</h2>
            <div class="detail-meta">
                <span class="badge badge-${bug.priority.toLowerCase()}">${bug.priority}</span>
                <span class="badge badge-${bug.status.toLowerCase()}">${formatStatus(bug.status)}</span>
            </div>
        </div>
        <div class="detail-body">${escapeHtml(bug.description) || '<em>No description provided.</em>'}</div>
        <dl class="detail-info">
            <dt>Reporter</dt><dd>${escapeHtml(bug.reporter) || '\u2014'}</dd>
            <dt>Assignee</dt><dd>${escapeHtml(bug.assignee) || '\u2014'}</dd>
            <dt>Created</dt><dd>${formatDateTime(bug.createdAt)}</dd>
            <dt>Updated</dt><dd>${formatDateTime(bug.updatedAt)}</dd>
        </dl>
    `;

    const commentsList = $('#commentsList');
    if (comments.length === 0) {
        commentsList.innerHTML = '<p class="activity-empty">No comments yet.</p>';
    } else {
        commentsList.innerHTML = comments.map(c => `
            <div class="comment-item">
                <div class="comment-header">
                    <span class="comment-author">${escapeHtml(c.author || 'Anonymous')}</span>
                    <span class="comment-time">${formatDateTime(c.createdAt)}</span>
                </div>
                <div class="comment-body">${escapeHtml(c.body)}</div>
            </div>
        `).join('');
    }

    const commentForm = $('#commentForm');
    if (isLoggedIn()) {
        commentForm.classList.remove('hidden');
        const user = getUser();
        if (user) $('#commentAuthor').value = user.username;
    } else {
        commentForm.classList.add('hidden');
    }

    const activityList = $('#bugActivityList');
    if (activity.length === 0) {
        activityList.innerHTML = '<p class="activity-empty">No activity recorded.</p>';
    } else {
        activityList.innerHTML = activity.map(a => `
            <div class="activity-item">
                <span class="activity-action ${a.action}">${a.action}</span>
                <span class="activity-details">${escapeHtml(a.details)}</span>
                <span class="activity-time">${formatDateTime(a.timestamp)}</span>
            </div>
        `).join('');
    }
}

function closeModal() {
    $('#bugDetailModal').classList.add('hidden');
}

async function handleCommentSubmit(e) {
    e.preventDefault();
    if (!isLoggedIn()) return;
    const bugId = $('#bugDetailModal').dataset.bugId;
    const payload = {
        body: $('#commentBody').value,
        author: $('#commentAuthor').value || null
    };

    try {
        const res = await authFetch(`${API}/${bugId}/comments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            $('#commentBody').value = '';
            viewBug(bugId);
        }
    } catch (err) {
        console.error('Failed to post comment:', err);
    }
}

// ---------- DASHBOARD ----------

async function loadDashboard() {
    try {
        const res = await authFetch(`${API}/stats`);
        const stats = await res.json();
        renderDashboard(stats);
    } catch (err) {
        console.error('Failed to load dashboard:', err);
    }
}

function renderDashboard(stats) {
    const grid = $('#statsGrid');
    const open = stats.byStatus.OPEN || 0;
    const inProgress = stats.byStatus.IN_PROGRESS || 0;
    const resolved = stats.byStatus.RESOLVED || 0;
    const closed = stats.byStatus.CLOSED || 0;

    grid.innerHTML = `
        <div class="stat-tile"><div class="stat-value">${stats.totalBugs}</div><div class="stat-label">Total Bugs</div></div>
        <div class="stat-tile"><div class="stat-value" style="color:#1d4ed8">${open}</div><div class="stat-label">Open</div></div>
        <div class="stat-tile"><div class="stat-value" style="color:#92400e">${inProgress}</div><div class="stat-label">In Progress</div></div>
        <div class="stat-tile"><div class="stat-value" style="color:#065f46">${resolved}</div><div class="stat-label">Resolved</div></div>
        <div class="stat-tile"><div class="stat-value" style="color:#475569">${closed}</div><div class="stat-label">Closed</div></div>
    `;

    renderBarChart('statusBars', stats.byStatus, {
        OPEN: '#3b82f6', IN_PROGRESS: '#f59e0b', RESOLVED: '#22c55e', CLOSED: '#94a3b8'
    });

    renderBarChart('priorityBars', stats.byPriority, {
        LOW: '#94a3b8', MEDIUM: '#f59e0b', HIGH: '#f97316', CRITICAL: '#ef4444'
    });
}

function renderBarChart(containerId, data, colors) {
    const container = $(`#${containerId}`);
    const max = Math.max(...Object.values(data), 1);
    container.innerHTML = Object.entries(data).map(([key, val]) => `
        <div class="bar-item">
            <span class="bar-label">${formatStatus(key)}</span>
            <div class="bar-track">
                <div class="bar-fill" style="width:${(val / max) * 100}%; background:${colors[key] || '#94a3b8'}"></div>
            </div>
            <span class="bar-count">${val}</span>
        </div>
    `).join('');
}

// ---------- GLOBAL ACTIVITY ----------

async function loadGlobalActivity() {
    try {
        const res = await authFetch('/api/activity');
        const activity = await res.json();
        const list = $('#activityList');

        if (activity.length === 0) {
            list.innerHTML = '<p class="activity-empty">No activity yet. Create a bug to get started.</p>';
            return;
        }

        list.innerHTML = activity.map(a => `
            <div class="activity-item">
                <span class="activity-action ${a.action}">${a.action}</span>
                <span class="activity-details">Bug #${a.bugId} — ${escapeHtml(a.details)}${a.performedBy ? ' (by ' + escapeHtml(a.performedBy) + ')' : ''}</span>
                <span class="activity-time">${formatDateTime(a.timestamp)}</span>
            </div>
        `).join('');
    } catch (err) {
        console.error('Failed to load activity:', err);
    }
}

// ---------- HELPERS ----------

function formatStatus(s) { return s.replace(/_/g, ' '); }

function formatDate(dateStr) {
    if (!dateStr) return '\u2014';
    return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatDateTime(dateStr) {
    if (!dateStr) return '\u2014';
    return new Date(dateStr).toLocaleString('en-US', {
        month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit'
    });
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
