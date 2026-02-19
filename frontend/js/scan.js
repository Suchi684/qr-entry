let scanMode = 'entry';
let members = [];

document.querySelectorAll('.scan-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    scanMode = btn.getAttribute('data-type');
    document.getElementById('currentMode').textContent = scanMode.charAt(0).toUpperCase() + scanMode.slice(1);
    renderTable(members);
  });
});

document.getElementById("searchInput").addEventListener("input", function () {
  const query = this.value.toLowerCase();
  const filtered = members.filter(m => m.name.toLowerCase().includes(query));
  renderTable(filtered);
});

function uploadExcel() {
  const file = document.getElementById("excelFile").files[0];
  if (!file) return alert("Please choose a file first!");

  const reader = new FileReader();
  reader.onload = function (e) {
    const data = new Uint8Array(e.target.result);
    const workbook = XLSX.read(data, { type: "array" });
    const sheet = workbook.Sheets[workbook.SheetNames[0]];
    const raw = XLSX.utils.sheet_to_json(sheet);

    members = raw.map((r, i) => ({
      id: (r["S.No"] || i + 1).toString(),
      name: r["BB MEET PAID MEMBERS LIST"]?.trim() || `Member ${i + 1}`,
      scan: { entry: false, food: false, gift: false }
    }));

    localStorage.setItem("members", JSON.stringify(members));
    renderTable(members);
  };
  reader.readAsArrayBuffer(file);
}

function renderTable(data) {
  const table = document.getElementById("tableBody");
  table.innerHTML = "";

  data.forEach((m, i) => {
    const qrCanvas = document.createElement("canvas");
    QRCode.toCanvas(qrCanvas, m.name);

    const dlBtn = document.createElement("button");
    dlBtn.className = "btn btn-sm btn-primary";
    dlBtn.textContent = "Download";
    dlBtn.onclick = () => {
      const link = document.createElement("a");
      link.download = `${m.name.replace(/\\s+/g, '_')}_QR.png`;
      link.href = qrCanvas.toDataURL();
      link.click();
    };

    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${i + 1}</td>
      <td>${m.name}</td>
    `;

    const qrCell = document.createElement("td");
    qrCell.appendChild(qrCanvas);

    const dlCell = document.createElement("td");
    dlCell.appendChild(dlBtn);

    row.appendChild(qrCell);
    row.appendChild(dlCell);

    ["entry", "food", "gift"].forEach(mode => {
      const tick = m.scan[mode] ? "✅" : "❌";
      const td = document.createElement("td");
      td.textContent = tick;
      row.appendChild(td);
    });

    table.appendChild(row);
  });
}

function exportToExcel() {
  const data = members.map((m, i) => ({
    SNo: i + 1,
    Name: m.name,
    Entry: m.scan.entry ? "Yes" : "No",
    Food: m.scan.food ? "Yes" : "No",
    Gift: m.scan.gift ? "Yes" : "No"
  }));

  const ws = XLSX.utils.json_to_sheet(data);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, "Members");
  XLSX.writeFile(wb, "BB_Meet_Export.xlsx");
}

// Restore from localStorage on load
window.onload = () => {
  const stored = localStorage.getItem("members");
  if (stored) {
    members = JSON.parse(stored);
    renderTable(members);
  }

  new Html5QrcodeScanner("qr-reader", { fps: 10, qrbox: 250 }).render(onScanSuccess);
};

function onScanSuccess(decodedText) {
  const found = members.find(m => m.name === decodedText.trim());

  if (!found) {
    Swal.fire({
      icon: "error",
      title: "Name Not Found",
      text: "Scanned name does not match any member in the list.",
      confirmButtonColor: "#d33"
    });
    return;
  }

  if (found.scan[scanMode]) {
    Swal.fire({
      icon: "info",
      title: "Already Scanned",
      text: `${scanMode.charAt(0).toUpperCase() + scanMode.slice(1)} already marked for ${found.name}.`,
      confirmButtonColor: "#3085d6"
    });
    return;
  }

  found.scan[scanMode] = true;
  localStorage.setItem("members", JSON.stringify(members));

  Swal.fire({
    icon: "success",
    title: "Scan Successful",
    text: `${scanMode.charAt(0).toUpperCase() + scanMode.slice(1)} marked for ${found.name}`,
    confirmButtonColor: "#28a745"
  });

  renderTable(members);
}
