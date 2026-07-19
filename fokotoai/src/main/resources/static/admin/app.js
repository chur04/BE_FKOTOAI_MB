const API_BASE = '/FKOTOAI';
let allStudents = [];
let allStudentsFiltered = [];
let allChapters = [];
let selectedChapterId = null;
let revenueChartInstance = null;
let studentLevelChartInstance = null;
let examParticipationChartInstance = null;
let quizPassFailChartInstance = null;
let vocabLevelChartInstance = null;
let currentVocabs = [];
let studentPage = 1;
let vocabPage = 1;
const ITEMS_PER_PAGE = 15;

// On startup
document.addEventListener('DOMContentLoaded', () => {
  const token = localStorage.getItem('admin_token');
  if (token) {
    showApp();
  } else {
    showAuth();
  }
});

// Routing/Tab switching
function showAuth() {
  document.getElementById('auth-section').style.display = 'flex';
  document.getElementById('app-section').style.display = 'none';
}

function showApp() {
  document.getElementById('auth-section').style.display = 'none';
  document.getElementById('app-section').style.display = 'flex';
  loadDashboardData();
}

function switchTab(tabName) {
  // Update sidebar active state
  document.querySelectorAll('.menu-item').forEach(item => {
    item.classList.remove('active');
  });
  event.currentTarget.classList.add('active');

  // Update panels display
  document.querySelectorAll('.tab-panel').forEach(panel => {
    panel.style.display = 'none';
  });
  document.getElementById(`tab-${tabName}`).style.display = 'block';

  // Update title
  const titles = {
    dashboard: 'Tổng quan hệ thống',
    students: 'Quản lý Học sinh',
    content: 'Quản lý Chương học & Từ vựng',
    'kanji-grammar': 'Quản lý Kanji & Ngữ pháp N5/N4',
    exams: 'Quản lý Đề thi & Câu hỏi',
    packages: 'Quản lý Gói học VIP & Giao dịch',
    feedback: 'Quản lý Góp ý & Báo cáo lỗi từ App'
  };
  document.getElementById('tab-title').innerText = titles[tabName];

  // Load specific data
  if (tabName === 'dashboard') {
    loadDashboardData();
  } else if (tabName === 'students') {
    loadStudents();
  } else if (tabName === 'content') {
    loadChapters();
  } else if (tabName === 'exams') {
    loadExamTemplates();
    loadExamCategories();
  } else if (tabName === 'packages') {
    loadPackages();
    loadTransactions();
  } else if (tabName === 'kanji-grammar') {
    renderKanjiTable(1);
    renderGrammarTable(1);
  }
}

// Authentication
async function handleLogin(event) {
  event.preventDefault();
  const usernameOrEmail = document.getElementById('usernameOrEmail').value.trim();
  const password = document.getElementById('password').value;
  const errorDiv = document.getElementById('login-error');
  errorDiv.style.display = 'none';

  try {
    const res = await fetch(`${API_BASE}/authen/admin/log-in`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ usernameOrEmail, password })
    });
    const data = await res.json();
    if (res.ok && data.code === 8386) {
      localStorage.setItem('admin_token', data.result);
      showToast('Đăng nhập thành công!');
      showApp();
    } else {
      errorDiv.innerText = data.message || 'Sai tài khoản hoặc mật khẩu!';
      errorDiv.style.display = 'block';
    }
  } catch (err) {
    errorDiv.innerText = 'Lỗi kết nối Server!';
    errorDiv.style.display = 'block';
  }
}

function handleLogout() {
  localStorage.removeItem('admin_token');
  showToast('Đã đăng xuất!');
  showAuth();
}

// Helper: Toast alerts
function showToast(message) {
  const toast = document.getElementById('toast');
  toast.innerText = message;
  toast.classList.add('show');
  setTimeout(() => {
    toast.classList.remove('show');
  }, 3000);
}

// Stats loaders
async function loadDashboardData() {
  try {
    let stats = {};
    const res = await fetch(`${API_BASE}/admin/dashboard-stats`);
    const data = await res.json();
    if (data.code === 8386) {
      stats = data.result;
      document.getElementById('stat-total-students').innerText = stats.totalStudents;
      document.getElementById('stat-active-students').innerText = stats.activeStudents;
      document.getElementById('stat-total-exams').innerText = stats.totalExamTemplates || 0;
      document.getElementById('stat-total-questions').innerText = stats.totalQuestions || 0;
      document.getElementById('stat-total-packages').innerText = stats.totalPackages;
      document.getElementById('stat-total-revenue').innerText = (stats.totalRevenue || 0).toLocaleString('vi-VN') + ' đ';
    }

    // Load recent students
    const resStudents = await fetch(`${API_BASE}/admin/students`);
    const dataStudents = await resStudents.json();
    const levels = { 'N5': 0, 'N4': 0, 'N3': 0, 'N2': 0, 'N1': 0 };
    if (dataStudents.code === 8386) {
      const students = dataStudents.result.slice(-5).reverse(); // Last 5 registered
      const tbody = document.getElementById('dashboard-recent-students');
      tbody.innerHTML = students.map(s => `
        <tr>
          <td><strong>${escapeHtml(s.fullname)}</strong></td>
          <td>${escapeHtml(s.username)}</td>
          <td>${escapeHtml(s.email)}</td>
          <td><span class="badge ${s.currentLevel === 'N4' ? 'badge-warning' : 'badge-success'}">${s.currentLevel}</span></td>
          <td>${s.createdAt ? new Date(s.createdAt).toLocaleDateString('vi-VN') : '-'}</td>
        </tr>
      `).join('');

      dataStudents.result.forEach(s => {
        const lvl = String(s.currentLevel || 'N5').toUpperCase();
        if (levels[lvl] !== undefined) {
          levels[lvl]++;
        } else {
          levels[lvl] = 1;
        }
      });
    }

    // Load recent transactions
    const resTx = await fetch(`${API_BASE}/admin/payment-transactions`);
    const dataTx = await resTx.json();
    const packageRevenue = {};
    if (dataTx.code === 8386) {
      const txs = dataTx.result.slice(0, 5); // Already ordered desc in backend
      const tbody = document.getElementById('dashboard-recent-transactions');
      if (txs.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 12px;">Chưa có giao dịch nào.</td></tr>`;
      } else {
        tbody.innerHTML = txs.map(t => `
          <tr>
            <td>
              <strong>${escapeHtml(t.studentName || 'Ẩn danh')}</strong>
              <div style="font-size: 11px; color: var(--text-muted);">${escapeHtml(t.studentEmail || '')}</div>
            </td>
            <td><span class="badge badge-success" style="font-weight: 700;">${escapeHtml(t.packageCode || '')}</span></td>
            <td><strong>${(t.amount || 0).toLocaleString('vi-VN')} đ</strong></td>
            <td>
              <span class="badge ${t.status === 'SUCCESS' ? 'badge-success' : (t.status === 'FAILED' ? 'badge-danger' : 'badge-warning')}">
                ${t.status}
              </span>
            </td>
          </tr>
        `).join('');
      }

      dataTx.result.forEach(t => {
        if (t.status === 'SUCCESS') {
          const code = t.packageName || t.packageCode || 'Gói khác';
          packageRevenue[code] = (packageRevenue[code] || 0) + (t.amount || 0);
        }
      });
    }

    // Initialize Charts
    const revCanvas = document.getElementById('revenueChart');
    if (revCanvas) {
      if (revenueChartInstance) {
        revenueChartInstance.destroy();
      }
      const labels = Object.keys(packageRevenue);
      const values = Object.values(packageRevenue);
      revenueChartInstance = new Chart(revCanvas, {
        type: 'bar',
        data: {
          labels: labels.length > 0 ? labels : ['Chưa có dữ liệu'],
          datasets: [{
            label: 'Doanh thu (VNĐ)',
            data: values.length > 0 ? values : [0],
            backgroundColor: [
              'rgba(79, 70, 229, 0.75)',
              'rgba(16, 185, 129, 0.75)',
              'rgba(245, 158, 11, 0.75)',
              'rgba(239, 68, 68, 0.75)'
            ],
            borderColor: [
              '#4f46e5',
              '#10b981',
              '#f59e0b',
              '#ef4444'
            ],
            borderWidth: 1.5,
            borderRadius: 6
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          indexAxis: 'y',
          plugins: {
            legend: { display: false }
          },
          scales: {
            x: {
              beginAtZero: true,
              grid: { color: 'rgba(0, 0, 0, 0.04)' }
            },
            y: {
              grid: { display: false }
            }
          }
        }
      });
    }

    const levelCanvas = document.getElementById('studentLevelChart');
    if (levelCanvas) {
      if (studentLevelChartInstance) {
        studentLevelChartInstance.destroy();
      }
      const activeLevels = {};
      Object.entries(levels).forEach(([lvl, val]) => {
        if (val > 0) activeLevels[lvl] = val;
      });
      const labels = Object.keys(activeLevels);
      const values = Object.values(activeLevels);

      studentLevelChartInstance = new Chart(levelCanvas, {
        type: 'doughnut',
        data: {
          labels: labels.length > 0 ? labels : ['Chưa có học sinh'],
          datasets: [{
            data: values.length > 0 ? values : [1],
            backgroundColor: [
              '#3182ce',
              '#38a169',
              '#dd6b20',
              '#e53e3e',
              '#805ad5'
            ],
            borderWidth: 2,
            borderColor: 'var(--card-bg)'
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'bottom',
              labels: {
                boxWidth: 10,
                font: { size: 11, weight: 600 }
              }
            }
          },
          cutout: '65%'
        }
      });
    }

    // Fetch and Initialize Exam Participation Chart
    try {
      const resParticipation = await fetch(`${API_BASE}/admin/exam-participation`);
      const dataParticipation = await resParticipation.json();
      if (dataParticipation.code === 8386) {
        const participations = dataParticipation.result;
        const partCanvas = document.getElementById('examParticipationChart');
        if (partCanvas) {
          if (examParticipationChartInstance) {
            examParticipationChartInstance.destroy();
          }
          const labels = participations.map(p => p.templateName);
          const values = participations.map(p => p.participationPercentage);

          examParticipationChartInstance = new Chart(partCanvas, {
            type: 'bar',
            data: {
              labels: labels.length > 0 ? labels : ['Chưa có đề thi'],
              datasets: [{
                label: 'Tỷ lệ tham gia (%)',
                data: values.length > 0 ? values : [0],
                backgroundColor: 'rgba(237, 100, 166, 0.75)',
                borderColor: '#ed64a6',
                borderWidth: 1.5,
                borderRadius: 6
              }]
            },
            options: {
              responsive: true,
              maintainAspectRatio: false,
              plugins: {
                legend: { display: false }
              },
              scales: {
                y: {
                  beginAtZero: true,
                  max: 100,
                  ticks: {
                    callback: function(value) { return value + "%" }
                  },
                  grid: { color: 'rgba(0, 0, 0, 0.04)' }
                },
                x: {
                  grid: { display: false }
                }
              }
            }
          });
        }
      }
    } catch (errPart) {
      console.error('Lỗi khi load biểu đồ tỷ lệ tham gia đề thi', errPart);
    }

    // Render Quiz Pass/Fail Chart
    const pfCanvas = document.getElementById('quizPassFailChart');
    if (pfCanvas) {
      if (quizPassFailChartInstance) {
        quizPassFailChartInstance.destroy();
      }
      const totalPass = stats.totalPassAttempts || 0;
      const totalFail = stats.totalFailAttempts || 0;
      const hasAttempts = (totalPass + totalFail) > 0;

      quizPassFailChartInstance = new Chart(pfCanvas, {
        type: 'doughnut',
        data: {
          labels: hasAttempts ? ['Đạt (Pass)', 'Trượt (Fail)'] : ['Chưa có lượt thi'],
          datasets: [{
            data: hasAttempts ? [totalPass, totalFail] : [1],
            backgroundColor: hasAttempts ? ['#10b981', '#ef4444'] : ['#e2e8f0'],
            borderWidth: 2,
            borderColor: 'var(--card-bg)'
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'bottom',
              labels: {
                boxWidth: 10,
                font: { size: 11, weight: 600 }
              }
            }
          },
          cutout: '65%'
        }
      });
    }

    // Render Vocab Level Chart
    const vocabCanvas = document.getElementById('vocabLevelChart');
    if (vocabCanvas) {
      if (vocabLevelChartInstance) {
        vocabLevelChartInstance.destroy();
      }
      const counts = stats.vocabCountByLevel || {};
      const levelsOrder = ['N5', 'N4', 'N3', 'N2', 'N1'];
      const values = levelsOrder.map(l => counts[l] || 0);

      vocabLevelChartInstance = new Chart(vocabCanvas, {
        type: 'bar',
        data: {
          labels: levelsOrder,
          datasets: [{
            label: 'Số lượng từ vựng',
            data: values,
            backgroundColor: 'rgba(54, 162, 235, 0.75)',
            borderColor: '#36a2eb',
            borderWidth: 1.5,
            borderRadius: 6
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          },
          scales: {
            y: {
              beginAtZero: true,
              grid: { color: 'rgba(0, 0, 0, 0.04)' }
            },
            x: {
              grid: { display: false }
            }
          }
        }
      });
    }

    // Render Leaderboard (Top Students)
    const leaderboardDiv = document.getElementById('dashboard-leaderboard');
    if (leaderboardDiv && dataStudents.code === 8386) {
      const sortedStudents = [...dataStudents.result]
        .sort((a, b) => (b.rankPoints || 0) - (a.rankPoints || 0))
        .slice(0, 5);

      leaderboardDiv.innerHTML = sortedStudents.map((s, index) => {
        const rankColors = ['#f59e0b', '#718096', '#b45309', '#4f46e5', '#10b981'];
        const medal = index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : `#${index + 1}`;
        return `
          <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: rgba(0, 0, 0, 0.015); border-radius: 10px; border-left: 4px solid ${rankColors[index] || '#cbd5e0'}">
            <div style="display: flex; align-items: center; gap: 12px;">
              <span style="font-size: 16px; font-weight: 800; min-width: 24px; text-align: center;">${medal}</span>
              <img src="${s.avatarUrl || 'https://www.w3schools.com/howto/img_avatar.png'}" style="width: 32px; height: 32px; border-radius: 50%; object-fit: cover; border: 1px solid rgba(0,0,0,0.1);" onerror="this.src='https://www.w3schools.com/howto/img_avatar.png'" />
              <div>
                <div style="font-size: 13px; font-weight: 700; color: var(--text-main);">${escapeHtml(s.fullname)}</div>
                <div style="font-size: 11px; color: var(--text-muted);">@${escapeHtml(s.username)}</div>
              </div>
            </div>
            <div style="text-align: right;">
              <span style="font-size: 14px; font-weight: 800; color: var(--text-main);">${s.rankPoints || 0}</span>
              <div style="font-size: 10px; color: var(--text-muted); font-weight: 600; text-transform: uppercase;">Điểm</div>
            </div>
          </div>
        `;
      }).join('');
    }
  } catch (err) {
    console.error('Lỗi tải dữ liệu dashboard', err);
  }
}

// Students Management
async function loadStudents() {
  try {
    const res = await fetch(`${API_BASE}/admin/students`);
    const data = await res.json();
    if (data.code === 8386) {
      allStudents = data.result;
      allStudentsFiltered = [...allStudents];
      studentPage = 1;
      renderStudentsTable();
    }
  } catch (err) {
    showToast('Không thể kết nối đến máy chủ!');
  }
}

function renderStudentsTable() {
  const tbody = document.getElementById('students-table-body');
  if (allStudentsFiltered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">Không tìm thấy học sinh nào.</td></tr>`;
    document.getElementById('students-pagination').innerHTML = '';
    return;
  }

  const totalItems = allStudentsFiltered.length;
  const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE);
  if (studentPage > totalPages) studentPage = totalPages;
  if (studentPage < 1) studentPage = 1;

  const startIndex = (studentPage - 1) * ITEMS_PER_PAGE;
  const endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);
  const paginatedStudents = allStudentsFiltered.slice(startIndex, endIndex);

  tbody.innerHTML = paginatedStudents.map(s => {
    const isActive = s.status === 'ACTIVE';
    const statusBadge = isActive 
      ? '<span class="badge badge-success">Đang hoạt động</span>' 
      : '<span class="badge badge-danger">Đã khóa</span>';
    const actionBtn = isActive
      ? `<button class="btn btn-danger" style="padding: 6px 12px; font-size: 13px;" onclick="changeStudentStatus(${s.studentId}, 'INACTIVE')">Khóa tài khoản</button>`
      : `<button class="btn btn-primary" style="padding: 6px 12px; font-size: 13px;" onclick="changeStudentStatus(${s.studentId}, 'ACTIVE')">Kích hoạt</button>`;

    return `
      <tr>
        <td>${s.studentId}</td>
        <td><strong>${escapeHtml(s.fullname)}</strong></td>
        <td>${escapeHtml(s.username)}</td>
        <td>${escapeHtml(s.email)}</td>
        <td><span class="badge ${s.currentLevel === 'N4' ? 'badge-warning' : 'badge-success'}">${s.currentLevel}</span></td>
        <td><strong>${s.rankPoints}</strong> pts</td>
        <td>${statusBadge}</td>
        <td>${actionBtn}</td>
      </tr>
    `;
  }).join('');

  renderPaginationControls('students-pagination', totalPages, studentPage, (page) => {
    studentPage = page;
    renderStudentsTable();
  });
}

function filterStudents() {
  const query = document.getElementById('student-search').value.toLowerCase().trim();
  if (!query) {
    allStudentsFiltered = [...allStudents];
  } else {
    allStudentsFiltered = allStudents.filter(s => 
      s.fullname.toLowerCase().includes(query) || 
      s.username.toLowerCase().includes(query) || 
      s.email.toLowerCase().includes(query)
    );
  }
  studentPage = 1;
  renderStudentsTable();
}

async function changeStudentStatus(studentId, newStatus) {
  const actionText = newStatus === 'ACTIVE' ? 'kích hoạt' : 'khóa';
  if (!confirm(`Bạn có chắc chắn muốn ${actionText} tài khoản học sinh này?`)) {
    return;
  }
  try {
    const res = await fetch(`${API_BASE}/admin/students/${studentId}/status?status=${newStatus}`, {
      method: 'PUT'
    });
    const data = await res.json();
    if (data.code === 8386) {
      showToast('Đã cập nhật trạng thái học sinh!');
      loadStudents();
    }
  } catch (err) {
    showToast('Lỗi cập nhật trạng thái!');
  }
}

// Chapters Management
async function loadChapters() {
  try {
    const res = await fetch(`${API_BASE}/admin/chapters`);
    const data = await res.json();
    if (data.code === 8386) {
      allChapters = data.result;
      renderChaptersList(allChapters);
    }
  } catch (err) {
    showToast('Lỗi tải danh sách chương!');
  }
}

function renderChaptersList(chapters) {
  const container = document.getElementById('chapters-list');
  if (chapters.length === 0) {
    container.innerHTML = `<p style="text-align: center; padding: 20px; color: var(--text-muted);">Chưa có chương học nào.</p>`;
    return;
  }

  container.innerHTML = chapters.map(c => {
    const isSelected = selectedChapterId === c.chapterId ? 'selected' : '';
    return `
      <div class="chapter-item ${isSelected}" onclick="selectChapter(${c.chapterId}, '${escapeHtml(c.chapterName)}')">
        <h3>
          <span>${escapeHtml(c.chapterName)}</span>
          <div style="display: inline-flex; gap: 8px;">
            <button class="btn btn-secondary" style="padding: 4px 8px; font-size: 11px;" onclick="event.stopPropagation(); editChapter(${c.chapterId})">Sửa</button>
            <button class="btn btn-danger" style="padding: 4px 8px; font-size: 11px;" onclick="event.stopPropagation(); deleteChapter(${c.chapterId})">Xóa</button>
          </div>
        </h3>
        <p>${escapeHtml(c.description || 'Không có mô tả')}</p>
        <div class="chapter-meta">
          <span style="color: var(--primary-color);">Cấp độ: ${c.level}</span>
        </div>
      </div>
    `;
  }).join('');
}

function selectChapter(chapterId, title) {
  selectedChapterId = chapterId;
  document.getElementById('selected-chapter-title').innerText = title;
  document.getElementById('add-vocab-btn').disabled = false;
  
  // Highlight chapter item
  document.querySelectorAll('.chapter-item').forEach(item => {
    item.classList.remove('selected');
  });
  event.currentTarget.classList.add('selected');

  loadVocabularies(chapterId);
}

// Vocabulary Management
async function loadVocabularies(chapterId) {
  try {
    const res = await fetch(`${API_BASE}/admin/chapters/${chapterId}/items`);
    const data = await res.json();
    if (data.code === 8386) {
      currentVocabs = data.result;
      vocabPage = 1;
      renderVocabTable();
    }
  } catch (err) {
    showToast('Lỗi tải từ vựng!');
  }
}

function renderVocabTable() {
  const tbody = document.getElementById('vocab-table-body');
  if (currentVocabs.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">Chương này chưa có từ vựng nào. Click nút "+ Thêm từ vựng" để tạo mới!</td></tr>`;
    document.getElementById('vocab-pagination').innerHTML = '';
    return;
  }

  const totalItems = currentVocabs.length;
  const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE);
  if (vocabPage > totalPages) vocabPage = totalPages;
  if (vocabPage < 1) vocabPage = 1;

  const startIndex = (vocabPage - 1) * ITEMS_PER_PAGE;
  const endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);
  const paginatedVocabs = currentVocabs.slice(startIndex, endIndex);

  tbody.innerHTML = paginatedVocabs.map(v => `
    <tr>
      <td>
        <strong style="font-size: 16px; color: var(--primary-color);">${escapeHtml(v.word)}</strong>
        ${v.isKanji ? '<span class="badge badge-warning" style="font-size: 10px; margin-left: 4px; padding: 2px 6px;">Kanji</span>' : ''}
      </td>
      <td>${escapeHtml(v.reading)}</td>
      <td>${escapeHtml(v.meaning)}</td>
      <td><span style="color: var(--text-muted); font-size: 14px;">${escapeHtml(v.partOfSpeech || '-')}</span></td>
      <td>
        <div style="display: flex; gap: 8px;">
          <button class="btn btn-secondary" style="padding: 6px 12px; font-size: 12px;" onclick="editVocab(${JSON.stringify(v).replace(/"/g, '&quot;')})">Sửa</button>
          <button class="btn btn-danger" style="padding: 6px 12px; font-size: 12px;" onclick="deleteVocab(${v.vocabId})">Xóa</button>
        </div>
      </td>
    </tr>
  `).join('');

  renderPaginationControls('vocab-pagination', totalPages, vocabPage, (page) => {
    vocabPage = page;
    renderVocabTable();
  });
}

// Chapter Modals & Forms
function openChapterModal() {
  document.getElementById('chapter-form').reset();
  document.getElementById('edit-chapter-id').value = '';
  document.getElementById('chapter-modal-title').innerText = 'Thêm chương học mới';
  document.getElementById('chapter-modal').classList.add('show');
}

function closeChapterModal() {
  document.getElementById('chapter-modal').classList.remove('show');
}

async function editChapter(chapterId) {
  const c = allChapters.find(item => item.chapterId === chapterId);
  if (!c) return;

  document.getElementById('edit-chapter-id').value = c.chapterId;
  document.getElementById('chapter-title').value = c.chapterName;
  document.getElementById('chapter-description').value = c.description || '';
  document.getElementById('chapter-level').value = c.level;
  document.getElementById('chapter-order').value = c.orderIndex;

  document.getElementById('chapter-modal-title').innerText = 'Sửa thông tin chương học';
  document.getElementById('chapter-modal').classList.add('show');
}

async function saveChapter(event) {
  event.preventDefault();
  const id = document.getElementById('edit-chapter-id').value;
  const chapterName = document.getElementById('chapter-title').value.trim();
  const description = document.getElementById('chapter-description').value.trim();
  const level = document.getElementById('chapter-level').value;
  const orderIndex = parseInt(document.getElementById('chapter-order').value);

  const payload = { chapterName, description, level, orderIndex };
  const method = id ? 'PUT' : 'POST';
  const url = id ? `${API_BASE}/admin/chapters/${id}` : `${API_BASE}/admin/chapters`;

  try {
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.code === 8386) {
      showToast('Đã lưu thông tin chương học!');
      closeChapterModal();
      loadChapters();
    }
  } catch (err) {
    showToast('Lỗi khi lưu chương học!');
  }
}

async function deleteChapter(chapterId) {
  if (!confirm('Bạn có chắc chắn muốn xóa chương học này? Toàn bộ các mối liên kết từ vựng trong chương cũng sẽ bị xóa.')) return;
  try {
    const res = await fetch(`${API_BASE}/admin/chapters/${chapterId}`, {
      method: 'DELETE'
    });
    const data = await res.json();
    if (data.code === 8386) {
      showToast('Đã xóa chương học!');
      if (selectedChapterId === chapterId) {
        selectedChapterId = null;
        document.getElementById('selected-chapter-title').innerText = 'Chọn một chương';
        document.getElementById('add-vocab-btn').disabled = true;
        document.getElementById('vocab-table-body').innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">Vui lòng chọn một chương học ở bên trái để xem danh sách từ vựng.</td></tr>`;
      }
      loadChapters();
    }
  } catch (err) {
    showToast('Lỗi khi xóa chương học!');
  }
}

// Vocabulary Modals & Forms
function openVocabModal() {
  document.getElementById('vocab-form').reset();
  document.getElementById('edit-vocab-id').value = '';
  document.getElementById('vocab-modal-title').innerText = 'Thêm từ vựng mới';
  document.getElementById('vocab-modal').classList.add('show');
}

function closeVocabModal() {
  document.getElementById('vocab-modal').classList.remove('show');
}

function editVocab(v) {
  document.getElementById('edit-vocab-id').value = v.vocabId;
  document.getElementById('vocab-word').value = v.word;
  document.getElementById('vocab-reading').value = v.reading;
  document.getElementById('vocab-meaning').value = v.meaning;
  document.getElementById('vocab-pos').value = v.partOfSpeech || '';
  document.getElementById('vocab-level').value = v.level;
  document.getElementById('vocab-is-kanji').value = v.isKanji ? 'true' : 'false';
  document.getElementById('vocab-audio').value = v.audioUrl || '';
  document.getElementById('vocab-sentence').value = v.exampleSentence || '';
  document.getElementById('vocab-sentence-meaning').value = v.exampleMeaning || '';
  document.getElementById('vocab-stroke').value = v.strokeOrderUrl || '';
  document.getElementById('vocab-onyomi').value = v.onyomi || '';
  document.getElementById('vocab-kunyomi').value = v.kunyomi || '';

  document.getElementById('vocab-modal-title').innerText = 'Sửa thông tin từ vựng';
  document.getElementById('vocab-modal').classList.add('show');
}

async function saveVocab(event) {
  event.preventDefault();
  const id = document.getElementById('edit-vocab-id').value;
  const payload = {
    word: document.getElementById('vocab-word').value.trim(),
    reading: document.getElementById('vocab-reading').value.trim(),
    meaning: document.getElementById('vocab-meaning').value.trim(),
    partOfSpeech: document.getElementById('vocab-pos').value.trim(),
    level: document.getElementById('vocab-level').value,
    isKanji: document.getElementById('vocab-is-kanji').value === 'true',
    audioUrl: document.getElementById('vocab-audio').value.trim(),
    exampleSentence: document.getElementById('vocab-sentence').value.trim(),
    exampleMeaning: document.getElementById('vocab-sentence-meaning').value.trim(),
    strokeOrderUrl: document.getElementById('vocab-stroke').value.trim(),
    onyomi: document.getElementById('vocab-onyomi').value.trim(),
    kunyomi: document.getElementById('vocab-kunyomi').value.trim()
  };

  const method = id ? 'PUT' : 'POST';
  const url = id 
    ? `${API_BASE}/admin/vocabulary/${id}` 
    : `${API_BASE}/admin/vocabulary?chapterId=${selectedChapterId}`;

  try {
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.code === 8386) {
      showToast('Đã lưu thông tin từ vựng!');
      closeVocabModal();
      loadVocabularies(selectedChapterId);
    }
  } catch (err) {
    showToast('Lỗi khi lưu từ vựng!');
  }
}

async function deleteVocab(vocabId) {
  if (!confirm('Bạn có chắc chắn muốn xóa từ vựng này khỏi hệ thống?')) return;
  try {
    const res = await fetch(`${API_BASE}/admin/vocabulary/${vocabId}`, {
      method: 'DELETE'
    });
    const data = await res.json();
    if (data.code === 8386) {
      showToast('Đã xóa từ vựng!');
      loadVocabularies(selectedChapterId);
    }
  } catch (err) {
    showToast('Lỗi khi xóa từ vựng!');
  }
}

// Utility: Escape HTML to avoid XSS
function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
}

// Generic Pagination Renderer
function renderPaginationControls(containerId, totalPages, currentPage, onPageChange) {
  const container = document.getElementById(containerId);
  if (totalPages <= 1) {
    container.innerHTML = '';
    return;
  }

  let html = '';
  // Prev button
  html += `<button class="pagination-btn" ${currentPage === 1 ? 'disabled' : ''} onclick="window.${containerId}Change(${currentPage - 1})">Trước</button>`;

  // Page buttons
  for (let i = 1; i <= totalPages; i++) {
    if (i === currentPage) {
      html += `<button class="pagination-btn active">${i}</button>`;
    } else {
      html += `<button class="pagination-btn" onclick="window.${containerId}Change(${i})">${i}</button>`;
    }
  }

  // Next button
  html += `<button class="pagination-btn" ${currentPage === totalPages ? 'disabled' : ''} onclick="window.${containerId}Change(${currentPage + 1})">Sau</button>`;

  container.innerHTML = html;

  // Bind change handler globally so inline onclick works
  window[`${containerId}Change`] = onPageChange;
}

// --- NEW FEATURES: EXAMS & QUESTIONS ---
let allExamTemplates = [];
let allExamCategories = [];
let selectedExamId = null;
let currentQuestions = [];
let questionPage = 1;

async function loadExamCategories() {
  try {
    const res = await fetch(`${API_BASE}/admin/exam-categories`);
    const data = await res.json();
    if (data.code === 8386) {
      allExamCategories = data.result;
      const select = document.getElementById('template-category');
      select.innerHTML = allExamCategories.map(c => `
        <option value="${c.categoryId}">${escapeHtml(c.categoryName)} (${c.level})</option>
      `).join('');
    }
  } catch (err) {
    console.error('Lỗi tải danh mục đề thi', err);
  }
}

async function loadExamTemplates() {
  try {
    const res = await fetch(`${API_BASE}/admin/exam-templates`);
    const data = await res.json();
    if (data.code === 8386) {
      allExamTemplates = data.result;
      renderExamTemplates();
    }
  } catch (err) {
    showToast('Lỗi tải danh sách đề thi mẫu!');
  }
}

function renderExamTemplates() {
  const container = document.getElementById('exam-templates-list');
  if (allExamTemplates.length === 0) {
    container.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 20px;">Không có đề thi mẫu nào.</div>`;
    return;
  }

  container.innerHTML = allExamTemplates.map(t => {
    const isActive = t.templateId === selectedExamId;
    return `
      <div class="chapter-item ${isActive ? 'active' : ''}" onclick="selectExamTemplate(${t.templateId}, '${escapeHtml(t.templateName)}')">
        <h3 style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
          <span>${escapeHtml(t.templateName)}</span>
          <div style="display: inline-flex; gap: 8px;">
            <button class="btn btn-secondary" style="padding: 4px 8px; font-size: 11px;" onclick="event.stopPropagation(); editExamTemplate(${t.templateId})">Sửa</button>
            <button class="btn btn-danger" style="padding: 4px 8px; font-size: 11px;" onclick="event.stopPropagation(); deleteExamTemplate(${t.templateId})">Xóa</button>
          </div>
        </h3>
        <div class="chapter-meta">
          <span style="color: var(--primary-color);">Cấp độ: ${t.level}</span>
          <span style="color: var(--text-muted); font-size: 12px;">Số câu: ${t.totalQuestions} | Thời gian: ${t.timeLimitMinutes}p</span>
        </div>
      </div>
    `;
  }).join('');
}

function selectExamTemplate(templateId, title) {
  selectedExamId = templateId;
  document.getElementById('selected-exam-title').innerText = title;
  document.getElementById('add-question-btn').disabled = false;
  renderExamTemplates();
  loadQuestions(templateId);
}

async function loadQuestions(templateId) {
  try {
    const res = await fetch(`${API_BASE}/admin/exam-templates/${templateId}/questions`);
    const data = await res.json();
    if (data.code === 8386) {
      currentQuestions = data.result;
      questionPage = 1;
      renderQuestionsTable();
    }
  } catch (err) {
    showToast('Lỗi tải danh sách câu hỏi!');
  }
}

function renderQuestionsTable() {
  const tbody = document.getElementById('questions-table-body');
  if (currentQuestions.length === 0) {
    tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--text-muted);">Đề thi này chưa có câu hỏi nào. Click nút "+ Thêm câu hỏi" để tạo mới!</td></tr>`;
    document.getElementById('questions-pagination').innerHTML = '';
    return;
  }

  const totalItems = currentQuestions.length;
  const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE);
  if (questionPage > totalPages) questionPage = totalPages;
  if (questionPage < 1) questionPage = 1;

  const startIndex = (questionPage - 1) * ITEMS_PER_PAGE;
  const endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);
  const paginatedQuestions = currentQuestions.slice(startIndex, endIndex);

  tbody.innerHTML = paginatedQuestions.map(q => `
    <tr>
      <td>
        <div style="font-weight: 700; color: var(--text-main); margin-bottom: 6px;">${escapeHtml(q.questionText)}</div>
        <div style="font-size: 13px; color: var(--text-muted);">
          A: ${escapeHtml(q.optionA)} | B: ${escapeHtml(q.optionB)} | C: ${escapeHtml(q.optionC)} | D: ${escapeHtml(q.optionD)}
        </div>
        ${q.explanation ? `<div style="font-size: 12px; color: #718096; margin-top: 4px; font-style: italic;">Giải thích: ${escapeHtml(q.explanation)}</div>` : ''}
      </td>
      <td><span class="badge badge-success">${q.correctAnswer}</span></td>
      <td><span class="badge ${q.level === 'N4' ? 'badge-warning' : 'badge-success'}">${q.level}</span></td>
      <td>
        <div style="display: flex; gap: 8px;">
          <button class="btn btn-secondary" style="padding: 6px 12px; font-size: 13px;" onclick="editQuestion(${q.questionId})">Sửa</button>
          <button class="btn btn-danger" style="padding: 6px 12px; font-size: 13px;" onclick="deleteQuestion(${q.questionId})">Xóa</button>
        </div>
      </td>
    </tr>
  `).join('');

  renderPaginationControls('questions-pagination', totalPages, questionPage, (page) => {
    questionPage = page;
    renderQuestionsTable();
  });
}

// Modal Exam Template Actions
function openExamTemplateModal() {
  document.getElementById('exam-template-modal-title').innerText = 'Thêm đề thi mẫu mới';
  document.getElementById('edit-template-id').value = '';
  document.getElementById('exam-template-form').reset();
  document.getElementById('exam-template-modal').classList.add('show');
}

function closeExamTemplateModal() {
  document.getElementById('exam-template-modal').classList.remove('show');
}

function editExamTemplate(templateId) {
  const t = allExamTemplates.find(x => x.templateId === templateId);
  if (!t) return;
  document.getElementById('exam-template-modal-title').innerText = 'Chỉnh sửa đề thi mẫu';
  document.getElementById('edit-template-id').value = t.templateId;
  document.getElementById('template-name').value = t.templateName;
  document.getElementById('template-category').value = t.categoryId || '';
  document.getElementById('template-level').value = t.level || 'N5';
  document.getElementById('template-total-questions').value = t.totalQuestions || 20;
  document.getElementById('template-time-limit').value = t.timeLimitMinutes || 45;
  document.getElementById('template-passing-score').value = t.passingScore || 50;
  document.getElementById('template-shuffle-q').value = String(t.shuffleQuestions);
  document.getElementById('template-shuffle-o').value = String(t.shuffleOptions);
  document.getElementById('exam-template-modal').classList.add('show');
}

async function saveExamTemplate(event) {
  event.preventDefault();
  const id = document.getElementById('edit-template-id').value;
  const body = {
    templateName: document.getElementById('template-name').value.trim(),
    level: document.getElementById('template-level').value,
    totalQuestions: parseInt(document.getElementById('template-total-questions').value),
    timeLimitMinutes: parseInt(document.getElementById('template-time-limit').value),
    passingScore: parseFloat(document.getElementById('template-passing-score').value),
    shuffleQuestions: document.getElementById('template-shuffle-q').value === 'true',
    shuffleOptions: document.getElementById('template-shuffle-o').value === 'true',
    status: 'ACTIVE'
  };
  const categoryId = document.getElementById('template-category').value;

  const url = id ? `${API_BASE}/admin/exam-templates/${id}?categoryId=${categoryId}` : `${API_BASE}/admin/exam-templates?categoryId=${categoryId}`;
  const method = id ? 'PUT' : 'POST';

  try {
    const res = await fetch(url, {
      method: method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const data = await res.json();
    if (data.code === 8386) {
      showToast(id ? 'Cập nhật đề thi mẫu thành công!' : 'Tạo đề thi mẫu thành công!');
      closeExamTemplateModal();
      loadExamTemplates();
    }
  } catch (err) {
    showToast('Lỗi khi lưu đề thi mẫu!');
  }
}

async function deleteExamTemplate(templateId) {
  if (!confirm('Bạn có chắc chắn muốn xóa đề thi mẫu này? (Tất cả câu hỏi liên kết cũng sẽ bị ảnh hưởng)')) return;
  try {
    const res = await fetch(`${API_BASE}/admin/exam-templates/${templateId}`, {
      method: 'DELETE'
    });
    const data = await res.json();
    if (data.code === 8386) {
      showToast('Đã xóa đề thi mẫu!');
      if (selectedExamId === templateId) {
        selectedExamId = null;
        document.getElementById('selected-exam-title').innerText = 'Chọn một đề thi';
        document.getElementById('add-question-btn').disabled = true;
        document.getElementById('questions-table-body').innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--text-muted);">Vui lòng chọn một đề thi mẫu ở bên trái để xem danh sách câu hỏi.</td></tr>`;
        document.getElementById('questions-pagination').innerHTML = '';
      }
      loadExamTemplates();
    }
  } catch (err) {
    showToast('Lỗi khi xóa đề thi mẫu!');
  }
}

// Modal Question Actions
function openQuestionModal() {
  document.getElementById('question-modal-title').innerText = 'Thêm câu hỏi mới';
  document.getElementById('edit-question-id').value = '';
  document.getElementById('question-form').reset();
  document.getElementById('question-modal').classList.add('show');
}

function closeQuestionModal() {
  document.getElementById('question-modal').classList.remove('show');
}

function editQuestion(questionId) {
  const q = currentQuestions.find(x => x.questionId === questionId);
  if (!q) return;
  document.getElementById('question-modal-title').innerText = 'Chỉnh sửa câu hỏi';
  document.getElementById('edit-question-id').value = q.questionId;
  document.getElementById('question-text').value = q.questionText;
  document.getElementById('question-option-a').value = q.optionA;
  document.getElementById('question-option-b').value = q.optionB;
  document.getElementById('question-option-c').value = q.optionC;
  document.getElementById('question-option-d').value = q.optionD;
  document.getElementById('question-correct').value = q.correctAnswer;
  document.getElementById('question-level').value = q.level;
  document.getElementById('question-explanation').value = q.explanation || '';
  document.getElementById('question-image').value = q.questionImageUrl || '';
  document.getElementById('question-audio').value = q.audioUrl || '';
  document.getElementById('question-modal').classList.add('show');
}

async function saveQuestion(event) {
  event.preventDefault();
  const id = document.getElementById('edit-question-id').value;
  const body = {
    questionText: document.getElementById('question-text').value.trim(),
    optionA: document.getElementById('question-option-a').value.trim(),
    optionB: document.getElementById('question-option-b').value.trim(),
    optionC: document.getElementById('question-option-c').value.trim(),
    optionD: document.getElementById('question-option-d').value.trim(),
    correctAnswer: document.getElementById('question-correct').value,
    level: document.getElementById('question-level').value,
    explanation: document.getElementById('question-explanation').value.trim(),
    questionImageUrl: document.getElementById('question-image').value.trim() || null,
    audioUrl: document.getElementById('question-audio').value.trim() || null,
    status: 'ACTIVE'
  };

  // Find corresponding template to link to category
  const activeTemplate = allExamTemplates.find(t => t.templateId === selectedExamId);
  const categoryId = activeTemplate ? activeTemplate.categoryId : (allExamCategories[0] ? allExamCategories[0].categoryId : null);

  if (!categoryId) {
    showToast('Lỗi: Không tìm thấy danh mục hợp lệ!');
    return;
  }

  const url = id ? `${API_BASE}/admin/questions/${id}?categoryId=${categoryId}` : `${API_BASE}/admin/questions?templateId=${selectedExamId}&categoryId=${categoryId}`;
  const method = id ? 'PUT' : 'POST';

  try {
    const res = await fetch(url, {
      method: method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const data = await res.json();
    if (data.code === 8386) {
      showToast(id ? 'Cập nhật câu hỏi thành công!' : 'Thêm câu hỏi thành công!');
      closeQuestionModal();
      loadQuestions(selectedExamId);
    }
  } catch (err) {
    showToast('Lỗi khi lưu câu hỏi!');
  }
}

async function deleteQuestion(questionId) {
  if (!confirm('Bạn có chắc chắn muốn xóa câu hỏi này khỏi đề thi?')) return;
  try {
    const res = await fetch(`${API_BASE}/admin/questions/${questionId}`, {
      method: 'DELETE'
    });
    const data = await res.json();
    if (data.code === 8386) {
      showToast('Đã xóa câu hỏi!');
      loadQuestions(selectedExamId);
    }
  } catch (err) {
    showToast('Lỗi khi xóa câu hỏi!');
  }
}

// --- NEW FEATURES: PACKAGES & TRANSACTIONS ---
let allPackages = [];
let allTransactions = [];
let transactionPage = 1;

async function loadPackages() {
  try {
    const res = await fetch(`${API_BASE}/admin/subscription-packages`);
    const data = await res.json();
    if (data.code === 8386) {
      allPackages = data.result;
      renderPackagesTable();
    }
  } catch (err) {
    showToast('Lỗi tải danh sách gói VIP!');
  }
}

function renderPackagesTable() {
  const tbody = document.getElementById('packages-table-body');
  if (allPackages.length === 0) {
    tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 15px;">Chưa có gói VIP nào.</td></tr>`;
    return;
  }

  tbody.innerHTML = allPackages.map(p => `
    <tr>
      <td>
        <div style="font-weight: 700; color: var(--primary-color);">${escapeHtml(p.packageCode)}</div>
        <div style="font-size: 12px; color: var(--text-muted);">${escapeHtml(p.packageName)}</div>
      </td>
      <td><strong>${formatCurrency(p.price)}</strong></td>
      <td>${p.durationDays} ngày</td>
      <td>
        <button class="btn btn-secondary" style="padding: 4px 8px; font-size: 11px;" onclick="editPackage(${p.packageId})">Sửa</button>
      </td>
    </tr>
  `).join('');
}

async function loadTransactions() {
  try {
    const res = await fetch(`${API_BASE}/admin/payment-transactions`);
    const data = await res.json();
    if (data.code === 8386) {
      allTransactions = data.result;
      transactionPage = 1;
      renderTransactionsTable();
    }
  } catch (err) {
    showToast('Lỗi tải lịch sử giao dịch!');
  }
}

function renderTransactionsTable() {
  const tbody = document.getElementById('transactions-table-body');
  if (allTransactions.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 15px;">Chưa có giao dịch lịch sử nào.</td></tr>`;
    document.getElementById('transactions-pagination').innerHTML = '';
    return;
  }

  const totalItems = allTransactions.length;
  const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE);
  if (transactionPage > totalPages) transactionPage = totalPages;
  if (transactionPage < 1) transactionPage = 1;

  const startIndex = (transactionPage - 1) * ITEMS_PER_PAGE;
  const endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);
  const paginatedTransactions = allTransactions.slice(startIndex, endIndex);

  tbody.innerHTML = paginatedTransactions.map(t => {
    let statusClass = 'badge-secondary';
    let statusText = 'Đang xử lý';
    if (t.status === 'SUCCESS') {
      statusClass = 'badge-success';
      statusText = 'Thành công';
    } else if (t.status === 'FAILED') {
      statusClass = 'badge-danger';
      statusText = 'Thất bại';
    }

    return `
      <tr>
        <td>
          <div style="font-weight: 700;">${escapeHtml(t.studentName || 'Học sinh')}</div>
          <div style="font-size: 12px; color: var(--text-muted);">${escapeHtml(t.studentEmail || '-')}</div>
        </td>
        <td style="font-family: monospace; font-size: 13px;">${escapeHtml(t.vnpayTxnRef)}</td>
        <td><span class="badge badge-warning">${escapeHtml(t.packageCode)}</span></td>
        <td><strong>${formatCurrency(t.amount)}</strong></td>
        <td style="font-size: 13px;">${t.createdAt ? new Date(t.createdAt).toLocaleString('vi-VN') : '-'}</td>
        <td><span class="badge ${statusClass}">${statusText}</span></td>
      </tr>
    `;
  }).join('');

  renderPaginationControls('transactions-pagination', totalPages, transactionPage, (page) => {
    transactionPage = page;
    renderTransactionsTable();
  });
}

function openPackageModal() {
  document.getElementById('package-modal-title').innerText = 'Thêm gói VIP mới';
  document.getElementById('edit-package-id').value = '';
  document.getElementById('package-form').reset();
  document.getElementById('package-code').disabled = false;
  document.getElementById('package-modal').classList.add('show');
}

function closePackageModal() {
  document.getElementById('package-modal').classList.remove('show');
}

function editPackage(packageId) {
  const p = allPackages.find(x => x.packageId === packageId);
  if (!p) return;
  document.getElementById('package-modal-title').innerText = 'Chỉnh sửa gói VIP';
  document.getElementById('edit-package-id').value = p.packageId;
  document.getElementById('package-code').value = p.packageCode;
  document.getElementById('package-code').disabled = true;
  document.getElementById('package-name').value = p.packageName;
  document.getElementById('package-price').value = p.price;
  document.getElementById('package-duration').value = p.durationDays;
  document.getElementById('package-description').value = p.description || '';
  document.getElementById('package-status').value = p.status || 'ACTIVE';
  document.getElementById('package-modal').classList.add('show');
}

async function savePackage(event) {
  event.preventDefault();
  const id = document.getElementById('edit-package-id').value;
  const body = {
    packageCode: document.getElementById('package-code').value.trim(),
    packageName: document.getElementById('package-name').value.trim(),
    price: parseFloat(document.getElementById('package-price').value),
    durationDays: parseInt(document.getElementById('package-duration').value),
    description: document.getElementById('package-description').value.trim(),
    status: document.getElementById('package-status').value
  };

  const url = id ? `${API_BASE}/admin/subscription-packages/${id}` : `${API_BASE}/admin/subscription-packages`;
  const method = id ? 'PUT' : 'POST';

  try {
    const res = await fetch(url, {
      method: method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const data = await res.json();
    if (data.code === 8386) {
      showToast(id ? 'Cập nhật gói VIP thành công!' : 'Tạo gói VIP thành công!');
      closePackageModal();
      loadPackages();
    }
  } catch (err) {
    showToast('Lỗi khi lưu gói VIP!');
  }
}

// Format Currency helper
function formatCurrency(value) {
  if (value === null || value === undefined) return '-';
  return value.toLocaleString('vi-VN') + ' đ';
}

// ==========================================
// KANJI & GRAMMAR DYNAMIC DATA & PAGINATION
// ==========================================

// Global listener to close dropdowns when clicking outside
document.addEventListener('click', function(e) {
  if (!e.target.closest('.action-dropdown')) {
    document.querySelectorAll('.action-menu.show').forEach(m => m.classList.remove('show'));
  }
});

function toggleActionMenu(menuId, event) {
  if (event) event.stopPropagation();
  const targetMenu = document.getElementById(menuId);
  if (!targetMenu) return;
  const isAlreadyOpen = targetMenu.classList.contains('show');
  document.querySelectorAll('.action-menu.show').forEach(m => m.classList.remove('show'));
  if (!isAlreadyOpen) {
    targetMenu.classList.add('show');
  }
}

function viewItemDetail(name, type) {
  document.querySelectorAll('.action-menu.show').forEach(m => m.classList.remove('show'));
  alert(`[Chi tiết] Đang xem thông tin chi tiết ${type}: ${name}`);
}

function editItem(name, type) {
  document.querySelectorAll('.action-menu.show').forEach(m => m.classList.remove('show'));
  alert(`[Chỉnh sửa] Mở form cập nhật dữ liệu ${type}: ${name}`);
}

const MOCK_KANJI_DATA = [
  { id: '#KJ001', kanji: '日', hanviet: 'NHẬT', onyomi: 'NICHI, JITSU', kunyomi: 'hi, bi, ka', stroke: '4 nét', level: 'N5', example: '日本 (Nhật Bản), 日曜日 (Chủ nhật)' },
  { id: '#KJ002', kanji: '月', hanviet: 'NGUYỆT', onyomi: 'GETSU, GATSU', kunyomi: 'tsuki', stroke: '4 nét', level: 'N5', example: '月曜日 (Thứ hai), 今月 (Tháng này)' },
  { id: '#KJ003', kanji: '木', hanviet: 'MỘC', onyomi: 'MOKU, BOKU', kunyomi: 'ki', stroke: '4 nét', level: 'N5', example: '木曜日 (Thứ năm), 大木 (Cây lớn)' },
  { id: '#KJ004', kanji: '水', hanviet: 'THỦY', onyomi: 'SUI', kunyomi: 'mizu', stroke: '4 nét', level: 'N5', example: '水曜日 (Thứ tư), 水道 (Nước máy)' },
  { id: '#KJ005', kanji: '金', hanviet: 'KIM', onyomi: 'KIN, KON', kunyomi: 'kane', stroke: '8 nét', level: 'N5', example: '金曜日 (Thứ sáu), お金 (Tiền)' },
  { id: '#KJ006', kanji: '土', hanviet: 'THỔ', onyomi: 'TO, DO', kunyomi: 'tsuchi', stroke: '3 nét', level: 'N5', example: '土曜日 (Thứ bảy), 土地 (Đất đai)' },
  { id: '#KJ007', kanji: '山', hanviet: 'SƠN', onyomi: 'SAN', kunyomi: 'yama', stroke: '3 nét', level: 'N5', example: '富士山 (Núi Phú Sĩ), 山道 (Đường núi)' },
  { id: '#KJ008', kanji: '川', hanviet: 'XUYÊN', onyomi: 'SEN', kunyomi: 'kawa', stroke: '3 nét', level: 'N5', example: 'ナイル川 (Sông Nile), 川上 (Thượng nguồn)' },
  { id: '#KJ009', kanji: '田', hanviet: 'ĐIỀN', onyomi: 'DEN', kunyomi: 'ta', stroke: '5 nét', level: 'N5', example: '水田 (Ruộng nước), 田んぼ (Cánh đồng)' },
  { id: '#KJ010', kanji: '人', hanviet: 'NHÂN', onyomi: 'JIN, NIN', kunyomi: 'hito', stroke: '2 nét', level: 'N5', example: '日本人 (Người Nhật), 1人 (Một người)' },
  { id: '#KJ011', kanji: '口', hanviet: 'KHẨU', onyomi: 'KOU, KU', kunyomi: 'kuchi', stroke: '3 nét', level: 'N5', example: '入口 (Lối vào), 出口 (Lối ra)' },
  { id: '#KJ012', kanji: '車', hanviet: 'XA', onyomi: 'SHA', kunyomi: 'kuruma', stroke: '7 nét', level: 'N5', example: '電車 (Xe điện), 自動車 (Ô tô)' },
  { id: '#KJ013', kanji: '新', hanviet: 'TÂN', onyomi: 'SHIN', kunyomi: 'atara(shii)', stroke: '13 nét', level: 'N4', example: '新聞 (Báo chí), 新しい (Mới)' },
  { id: '#KJ014', kanji: '駅', hanviet: 'DỊCH', onyomi: 'EKI', kunyomi: '-', stroke: '14 nét', level: 'N4', example: '駅員 (Nhân viên nhà ga), 東京駅 (Ga Tokyo)' },
  { id: '#KJ015', kanji: '銀', hanviet: 'NGÂN', onyomi: 'GIN', kunyomi: '-', stroke: '14 nét', level: 'N4', example: '銀行 (Ngân hàng), 銀色 (Màu bạc)' },
  { id: '#KJ016', kanji: '病', hanviet: 'BỆNH', onyomi: 'BYOU', kunyomi: 'ya(mai)', stroke: '10 nét', level: 'N4', example: '病院 (Bệnh viện), 病気 (Bệnh tật)' },
  { id: '#KJ017', kanji: '院', hanviet: 'VIỆN', onyomi: 'IN', kunyomi: '-', stroke: '10 nét', level: 'N4', example: '大学院 (Cao học), 病院 (Bệnh viện)' },
  { id: '#KJ018', kanji: '館', hanviet: 'QUÁN', onyomi: 'KAN', kunyomi: 'yakata', stroke: '16 nét', level: 'N4', example: '図書館 (Thư viện), 映画館 (Rạp chiếu phim)' },
  { id: '#KJ019', kanji: '旅', hanviet: 'LỮ', onyomi: 'RYO', kunyomi: 'tabi', stroke: '10 nét', level: 'N4', example: '旅行 (Du lịch), 旅人 (Lữ khách)' },
  { id: '#KJ020', kanji: '社', hanviet: 'XÃ', onyomi: 'SHA', kunyomi: 'yashiro', stroke: '7 nét', level: 'N4', example: '会社 (Công ty), 社員 (Nhân viên)' },
  { id: '#KJ021', kanji: '校', hanviet: 'HIỆU', onyomi: 'KOU', kunyomi: '-', stroke: '10 nét', level: 'N4', example: '学校 (Trường học), 高校 (Trường cấp 3)' },
  { id: '#KJ022', kanji: '店', hanviet: 'TIẾM', onyomi: 'TEN', kunyomi: 'mise', stroke: '8 nét', level: 'N4', example: '店員 (Nhân viên bán hàng), 喫茶店 (Quán cà phê)' },
  { id: '#KJ023', kanji: '代', hanviet: 'ĐẠI', onyomi: 'DAI, TAI', kunyomi: 'ka(waru)', stroke: '5 nét', level: 'N4', example: '時代 (Thời đại), 部屋代 (Tiền phòng)' },
  { id: '#KJ024', kanji: '親', hanviet: 'THÂN', onyomi: 'SHIN', kunyomi: 'oya, shina(shii)', stroke: '16 nét', level: 'N4', example: '両親 (Bố mẹ), 親切 (Tốt bụng)' }
];

const MOCK_GRAMMAR_DATA = [
  { id: '#GM001', pattern: '～です / ～ではありません', meaning: 'Là / Không phải là...', level: 'N5', example: 'わたしは学生です。', status: 'Đã duyệt' },
  { id: '#GM002', pattern: 'V-てください', meaning: 'Hãy làm gì đó (Yêu cầu lịch sự)', level: 'N5', example: 'ここに名前を書いてください。', status: 'Đã duyệt' },
  { id: '#GM003', pattern: 'V-てもいいです', meaning: 'Được phép làm gì đó (Xin phép)', level: 'N5', example: '写真を撮ってもいいですか。', status: 'Đã duyệt' },
  { id: '#GM004', pattern: 'V-てはいけません', meaning: 'Cấm làm gì đó', level: 'N5', example: 'ここでたばこを吸ってはいけません。', status: 'Đã duyệt' },
  { id: '#GM005', pattern: 'V-たいです', meaning: 'Muốn làm gì đó', level: 'N5', example: '日本へ行きたいです。', status: 'Đã duyệt' },
  { id: '#GM006', pattern: '～へ 行きます/来ます/帰ります', meaning: 'Đi / Đến / Về đâu đó', level: 'N5', example: '明日、東京へ行きます。', status: 'Đã duyệt' },
  { id: '#GM007', pattern: '～から ～まで', meaning: 'Từ... đến...', level: 'N5', example: '9時から5時まで働きます。', status: 'Đã duyệt' },
  { id: '#GM008', pattern: '～と 一緒に V', meaning: 'Cùng làm gì đó với ai', level: 'N5', example: '友達と一緒に映画を見ます。', status: 'Đã duyệt' },
  { id: '#GM009', pattern: 'V-ましょう / V-ましょうか', meaning: 'Cùng làm gì nhé / Tôi làm giúp nhé', level: 'N5', example: 'ちょっと休みましょう。', status: 'Đã duyệt' },
  { id: '#GM010', pattern: '～が 好きです / 嫌いです', meaning: 'Thích / Ghét cái gì đó', level: 'N5', example: 'わたしは日本語が好きです。', status: 'Đã duyệt' },
  { id: '#GM011', pattern: '～が 上手です / 下手です', meaning: 'Giỏi / Dở về cái gì', level: 'N5', example: 'マリアさんは歌が上手です。', status: 'Đã duyệt' },
  { id: '#GM012', pattern: '～があります / います', meaning: 'Có cái gì / Có ai đó ở đâu', level: 'N5', example: '部屋にテレビがあります。', status: 'Đã duyệt' },
  { id: '#GM013', pattern: 'V-たことがあります', meaning: 'Đã từng làm gì đó (Kinh nghiệm)', level: 'N4', example: '富士山に登ったことがあります。', status: 'Đã duyệt' },
  { id: '#GM014', pattern: 'V-ほうがいいです', meaning: 'Nên làm gì đó (Lời khuyên)', level: 'N4', example: '薬を飲んだほうがいいです。', status: 'Đã duyệt' },
  { id: '#GM015', pattern: 'V-すぎる', meaning: 'Làm gì đó quá mức', level: 'N4', example: '昨夜、お酒を飲みすぎました。', status: 'Đã duyệt' },
  { id: '#GM016', pattern: 'V-やすい / V-にくい', meaning: 'Dễ làm / Khó làm gì đó', level: 'N4', example: 'このペンは使いやすいです。', status: 'Đã duyệt' },
  { id: '#GM017', pattern: '～とき', meaning: 'Khi / Lúc làm gì đó', level: 'N4', example: '暇なとき、水泳をします。', status: 'Đã duyệt' },
  { id: '#GM018', pattern: 'V-たら', meaning: 'Nếu / Sau khi làm gì đó', level: 'N4', example: '雨が降ったら、出かけません。', status: 'Đã duyệt' },
  { id: '#GM019', pattern: 'V-ても', meaning: 'Cho dù làm gì đó thì vẫn...', level: 'N4', example: '安くても、買いません。', status: 'Đã duyệt' },
  { id: '#GM020', pattern: 'V-つづける', meaning: 'Tiếp tục làm gì đó', level: 'N4', example: '雨が降りつづけています。', status: 'Đã duyệt' },
  { id: '#GM021', pattern: 'V-はじめます', meaning: 'Bắt đầu làm gì đó', level: 'N4', example: '本を読みはじめました。', status: 'Đã duyệt' },
  { id: '#GM022', pattern: 'V-おわります', meaning: 'Kết thúc / Làm xong gì đó', level: 'N4', example: '宿題をしおわりました。', status: 'Đã duyệt' },
  { id: '#GM023', pattern: '～ように なります', meaning: 'Trở nên có thể làm gì đó', level: 'N4', example: '日本語が話せるようになりました。', status: 'Đã duyệt' },
  { id: '#GM024', pattern: '～そう です', meaning: 'Có vẻ như / Nghe nói là...', level: 'N4', example: '雨が降りそうです。', status: 'Đã duyệt' }
];

function renderKanjiTable(page = 1) {
  const pageSize = 8;
  const levelFilter = document.getElementById('kanji-level-filter')?.value || '';
  
  let filtered = MOCK_KANJI_DATA;
  let totalDisplayCount = 184;
  
  if (levelFilter === 'N5') {
    filtered = MOCK_KANJI_DATA.filter(item => item.level === 'N5');
    totalDisplayCount = 89;
  } else if (levelFilter === 'N4') {
    filtered = MOCK_KANJI_DATA.filter(item => item.level === 'N4');
    totalDisplayCount = 95;
  }

  const totalPages = Math.ceil(totalDisplayCount / pageSize) || 1;
  const currentPage = Math.min(Math.max(1, page), totalPages);
  
  const maxPoolPages = Math.ceil(filtered.length / pageSize);
  const poolPage = ((currentPage - 1) % maxPoolPages) + 1;
  const start = (poolPage - 1) * pageSize;
  const pageData = filtered.slice(start, start + pageSize);

  const tbody = document.getElementById('kanji-table-body');
  if (!tbody) return;

  if (pageData.length === 0) {
    tbody.innerHTML = `<tr><td colspan="9" style="text-align:center; color:#6b7280; padding:20px;">Không tìm thấy Kanji nào.</td></tr>`;
  } else {
    tbody.innerHTML = pageData.map(item => `
      <tr>
        <td>${item.id}</td>
        <td><b style="font-size: 20px; color: var(--primary-color);">${item.kanji}</b></td>
        <td>${item.hanviet}</td>
        <td>${item.onyomi}</td>
        <td>${item.kunyomi}</td>
        <td>${item.stroke}</td>
        <td><span class="badge ${item.level === 'N5' ? 'badge-success' : 'badge-info'}" style="${item.level === 'N4' ? 'background: #3b82f6; color: white;' : ''}">${item.level}</span></td>
        <td>${item.example}</td>
        <td style="text-align: center;">
          <div class="action-dropdown">
            <button class="action-btn-dots" onclick="toggleActionMenu('kanji-menu-${item.id}', event)">⋮</button>
            <div id="kanji-menu-${item.id}" class="action-menu">
              <div class="action-menu-item" onclick="viewItemDetail('${item.kanji}', 'Kanji')">
                <span>👁️</span> Xem chi tiết
              </div>
              <div class="action-menu-item" onclick="editItem('${item.kanji}', 'Kanji')">
                <span>✏️</span> Chỉnh sửa
              </div>
            </div>
          </div>
        </td>
      </tr>
    `).join('');
  }

  const statEl = document.getElementById('stat-kanji-total');
  if (statEl) {
    statEl.innerHTML = `184 <span style="font-size: 13px; font-weight: normal; color: #6b7280;">(89 N5, 95 N4)</span>`;
  }

  const paginationEl = document.getElementById('kanji-pagination');
  if (paginationEl) {
    const dispStart = (currentPage - 1) * pageSize + 1;
    const dispEnd = Math.min(currentPage * pageSize, totalDisplayCount);
    paginationEl.innerHTML = `
      <span style="font-size: 14px; color: #6b7280;">Hiển thị ${dispStart}-${dispEnd} trong tổng số <b>${totalDisplayCount}</b> Kanji</span>
      <div style="display: flex; gap: 8px;">
        <button class="btn btn-sm btn-secondary" ${currentPage === 1 ? 'disabled' : ''} onclick="renderKanjiTable(${currentPage - 1})">Trang trước</button>
        <span style="padding: 4px 12px; font-weight: 600; align-self: center;">Trang ${currentPage} / ${totalPages}</span>
        <button class="btn btn-sm btn-secondary" ${currentPage === totalPages ? 'disabled' : ''} onclick="renderKanjiTable(${currentPage + 1})">Trang sau</button>
      </div>
    `;
  }
}

function renderGrammarTable(page = 1) {
  const pageSize = 8;
  const levelFilter = document.getElementById('grammar-level-filter')?.value || '';
  
  let filtered = MOCK_GRAMMAR_DATA;
  let totalDisplayCount = 142;

  if (levelFilter === 'N5') {
    filtered = MOCK_GRAMMAR_DATA.filter(item => item.level === 'N5');
    totalDisplayCount = 62;
  } else if (levelFilter === 'N4') {
    filtered = MOCK_GRAMMAR_DATA.filter(item => item.level === 'N4');
    totalDisplayCount = 80;
  }

  const totalPages = Math.ceil(totalDisplayCount / pageSize) || 1;
  const currentPage = Math.min(Math.max(1, page), totalPages);
  
  const maxPoolPages = Math.ceil(filtered.length / pageSize);
  const poolPage = ((currentPage - 1) % maxPoolPages) + 1;
  const start = (poolPage - 1) * pageSize;
  const pageData = filtered.slice(start, start + pageSize);

  const tbody = document.getElementById('grammar-table-body');
  if (!tbody) return;

  if (pageData.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:#6b7280; padding:20px;">Không tìm thấy mẫu Ngữ pháp nào.</td></tr>`;
  } else {
    tbody.innerHTML = pageData.map(item => `
      <tr>
        <td>${item.id}</td>
        <td><b>${item.pattern}</b></td>
        <td>${item.meaning}</td>
        <td><span class="badge ${item.level === 'N5' ? 'badge-success' : 'badge-info'}" style="${item.level === 'N4' ? 'background: #3b82f6; color: white;' : ''}">${item.level}</span></td>
        <td>${item.example}</td>
        <td style="text-align: center;">
          <div class="action-dropdown">
            <button class="action-btn-dots" onclick="toggleActionMenu('grammar-menu-${item.id}', event)">⋮</button>
            <div id="grammar-menu-${item.id}" class="action-menu">
              <div class="action-menu-item" onclick="viewItemDetail('${item.pattern}', 'Ngữ pháp')">
                <span>👁️</span> Xem chi tiết
              </div>
              <div class="action-menu-item" onclick="editItem('${item.pattern}', 'Ngữ pháp')">
                <span>✏️</span> Chỉnh sửa
              </div>
            </div>
          </div>
        </td>
      </tr>
    `).join('');
  }

  const statEl = document.getElementById('stat-grammar-total');
  if (statEl) {
    statEl.innerHTML = `142 <span style="font-size: 13px; font-weight: normal; color: #6b7280;">(62 N5, 80 N4)</span>`;
  }

  const paginationEl = document.getElementById('grammar-pagination');
  if (paginationEl) {
    const dispStart = (currentPage - 1) * pageSize + 1;
    const dispEnd = Math.min(currentPage * pageSize, totalDisplayCount);
    paginationEl.innerHTML = `
      <span style="font-size: 14px; color: #6b7280;">Hiển thị ${dispStart}-${dispEnd} trong tổng số <b>${totalDisplayCount}</b> Mẫu Ngữ pháp</span>
      <div style="display: flex; gap: 8px;">
        <button class="btn btn-sm btn-secondary" ${currentPage === 1 ? 'disabled' : ''} onclick="renderGrammarTable(${currentPage - 1})">Trang trước</button>
        <span style="padding: 4px 12px; font-weight: 600; align-self: center;">Trang ${currentPage} / ${totalPages}</span>
        <button class="btn btn-sm btn-secondary" ${currentPage === totalPages ? 'disabled' : ''} onclick="renderGrammarTable(${currentPage + 1})">Trang sau</button>
      </div>
    `;
  }
}
