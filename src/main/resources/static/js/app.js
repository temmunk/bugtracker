const API_URL = '/api/bugs';

const bugTableBody = document.getElementById('bugTableBody');
const bugForm = document.getElementById('bugForm');
const bugFormElement = document.getElementById('bugFormElement');
const formTitle = document.getElementById('formTitle');
const emptyState = document.getElementById('emptyState');
const bugTable = document.getElementById('bugTable');

const searchInput = document.getElementById('searchInput');
const searchBtn = document.getElementById('searchBtn');
const statusFilter = document.getElementById('statusFilter');
const priorityFilter = document.getElementById('priorityFilter');
const newBugBtn = document.getElementById('newBugBtn');
const cancelBtn = document.getElementById('cancelBtn');

newBugBtn.addEventListener('click', () => openForm());
cancelBtn.addEventListener('click', () => closeForm());
bugFormElement.addEventListener('submit', handleSubmit);
searchBtn.addEventListener('click', loadBugs);
searchInput.addEventListener('keydown', (e) => { if (e.key === 'Enter') loadBugs(); });
statusFilter.addEventListener('change', () => { priorityFilter.value = ''; loadBugs(); });
priorityFilter.addEventListener('change', () => { statusFilter.value = ''; loadBugs(); });

loadBugs();

async function loadBugs() {
    try {
        const params = new URLSearchParams();
        const search = searchInput.value.trim();
        const status = statusFilter.value;
        const priority = priorityFilter.value;

        if (search) params.set('search', search);
        else if (status) params.set('status', status);
        else if (priority) params.set('priority', priority);

        const query = params.toString();
        const res = await fetch(`${API_URL}${query ? '?' + query : ''}`);
        const bugs = await res.json();
        renderBugs(bugs);
    } catch (err) {
        console.error('Failed to load bugs:', err);
    }
}

function renderBugs(bugs) {
    bugTableBody.innerHTML = '';

    if (bugs.length === 0) {
        bugTable.classList.add('hidden');
        emptyState.classList.remove('hidden');
        return;
    }

    bugTable.classList.remove('hidden');
    emptyState.classList.add('hidden');

    bugs.forEach(bug => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>#${bug.id}</td>
            <td><strong>${escapeHtml(bug.title)}</strong></td>
            <td><span class="badge badge-${bug.priority.toLowerCase()}">${bug.priority}</span></td>
            <td><span class="badge badge-${bug.status.toLowerCase()}">${formatStatus(bug.status)}</span></td>
            <td>${escapeHtml(bug.assignee || '—')}</td>
            <td>${formatDate(bug.createdAt)}</td>
            <td class="actions">
                <button class="btn btn-edit" onclick="editBug(${bug.id})">Edit</button>
                <button class="btn btn-danger" onclick="deleteBug(${bug.id})">Delete</button>
            </td>
        `;
        bugTableBody.appendChild(row);
    });
}

function openForm(bug = null) {
    bugForm.classList.remove('hidden');

    if (bug) {
        formTitle.textContent = 'Edit Bug';
        document.getElementById('bugId').value = bug.id;
        document.getElementById('title').value = bug.title;
        document.getElementById('description').value = bug.description || '';
        document.getElementById('priority').value = bug.priority;
        document.getElementById('status').value = bug.status;
        document.getElementById('reporter').value = bug.reporter || '';
        document.getElementById('assignee').value = bug.assignee || '';
    } else {
        formTitle.textContent = 'Report a Bug';
        bugFormElement.reset();
        document.getElementById('bugId').value = '';
    }

    document.getElementById('title').focus();
}

function closeForm() {
    bugForm.classList.add('hidden');
    bugFormElement.reset();
    document.getElementById('bugId').value = '';
}

async function handleSubmit(e) {
    e.preventDefault();

    const bugId = document.getElementById('bugId').value;
    const payload = {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        priority: document.getElementById('priority').value,
        status: document.getElementById('status').value,
        reporter: document.getElementById('reporter').value,
        assignee: document.getElementById('assignee').value
    };

    try {
        const url = bugId ? `${API_URL}/${bugId}` : API_URL;
        const method = bugId ? 'PUT' : 'POST';

        const res = await fetch(url, {
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
    try {
        const res = await fetch(`${API_URL}/${id}`);
        const bug = await res.json();
        openForm(bug);
    } catch (err) {
        console.error('Failed to load bug:', err);
    }
}

async function deleteBug(id) {
    if (!confirm('Are you sure you want to delete this bug?')) return;

    try {
        await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
        loadBugs();
    } catch (err) {
        console.error('Failed to delete bug:', err);
    }
}

function formatStatus(status) {
    return status.replace('_', ' ');
}

function formatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
