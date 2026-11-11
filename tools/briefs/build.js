// Builds each day's brief.docx and hints.md from briefs.js + extras.js + course.json.
// Usage (from this folder): npm install && node build.js [day-number...]
//                           node build.js --check     (exit 1 if any brief or hints file is out of date)
const fs = require("fs");
const path = require("path");
const {
  Document, Packer, Paragraph, TextRun, ExternalHyperlink, Table, TableRow, TableCell,
  WidthType, ShadingType, BorderStyle, HeadingLevel, LevelFormat, AlignmentType,
} = require("docx");
const JSZip = require("jszip");
const briefs = require("./briefs");
const extras = require("./extras");
const manifest = require("../course/course.json").days;

// course.json lists days in order; look a day up by its number, and fail loudly if it isn't there
function manifestDay(day) {
  const d = manifest.find((x) => x.day === day);
  if (!d) throw new Error(`course.json has no day ${day}`);
  return d;
}

const ROOT = path.resolve(__dirname, "../..");

// The facts a {{token}} may use for one day; same names as tools/course/course.py facts().
function facts(day) {
  const d = manifestDay(day);
  const f = { day: d.day, folder: d.folder, title: d.title, "solution.tests": d.solution.tests };
  if (d.starter) Object.assign(f, { "starter.tests": d.starter.tests, "starter.failing": d.starter.failing.length });
  if (d.bruno) Object.assign(f, { "bruno.requests": d.bruno.requests, "bruno.failingOnStarter": d.bruno.failingOnStarter.length });
  return f;
}

// Replaces {{token}} with the day's facts. A token the day doesn't have stops the build.
function render(text, day) {
  const f = facts(day);
  return text.replace(/\{\{([a-zA-Z.]+)\}\}/g, (_, token) => {
    if (f[token] === undefined || f[token] === null) throw new Error(`day ${day}: no fact called {{${token}}}`);
    return String(f[token]);
  });
}

// Everything a brief shows about itself comes from course.json; briefs.js holds only the task text.
function fromManifest(entry) {
  const d = manifestDay(entry.day);
  return {
    ...entry,
    folder: d.folder,
    dayTitle: d.title,
    plain: d.plain,
    title: "Workshop: " + d.title,
    subtitle: `Day ${d.day} · ${d.tagline}`,
    glance: [
      ["Language / Build tool", `${d.glance.java} / ${d.glance.build}`],
      ["Framework", d.glance.framework],
      ["Database", d.glance.database],
      ["API client", d.glance.apiClient],
      ["Starting point", "`" + d.folder + "/starter`"],
    ],
  };
}
const FONT = "Aptos";
const CODE = "Consolas";
const PAGE_WIDTH = 12240 - 2 * 1440; // US Letter minus 1" margins, in DXA

// Inline markup: **bold**, `code`, [text](url)
function runs(text) {
  const out = [];
  const re = /(\*\*[^*]+\*\*|`[^`]+`|\[[^\]]+\]\([^)]+\))/g;
  let last = 0;
  for (const m of text.matchAll(re)) {
    if (m.index > last) out.push(new TextRun({ text: text.slice(last, m.index), font: FONT }));
    const t = m[0];
    if (t.startsWith("**")) out.push(new TextRun({ text: t.slice(2, -2), bold: true, font: FONT }));
    else if (t.startsWith("`")) out.push(new TextRun({ text: t.slice(1, -1), font: CODE, size: 20 }));
    else {
      const [, label, url] = t.match(/\[([^\]]+)\]\(([^)]+)\)/);
      out.push(new ExternalHyperlink({ link: url, children: [new TextRun({ text: label, style: "Hyperlink", font: FONT })] }));
    }
    last = m.index + t.length;
  }
  if (last < text.length) out.push(new TextRun({ text: text.slice(last), font: FONT }));
  return out;
}

const border = { style: BorderStyle.SINGLE, size: 4, color: "BFBFBF" };
const borders = { top: border, bottom: border, left: border, right: border };

function table(rows, widths, headless = false) {
  if (headless) rows = rows.slice(1);
  const cols = rows[0].length;
  const w = widths || Array(cols).fill(Math.floor(PAGE_WIDTH / cols));
  const total = w.reduce((a, b) => a + b, 0);
  return new Table({
    width: { size: total, type: WidthType.DXA },
    columnWidths: w,
    rows: rows.map((row, r) => new TableRow({
      tableHeader: !headless && r === 0,
      children: row.map((cell, c) => new TableCell({
        borders,
        width: { size: w[c], type: WidthType.DXA },
        shading: !headless && r === 0 ? { fill: "E8EEF4", type: ShadingType.CLEAR, color: "auto" } : headless && c === 0 ? { fill: "F2F2F2", type: ShadingType.CLEAR, color: "auto" } : undefined,
        margins: { top: 60, bottom: 60, left: 100, right: 100 },
        children: [new Paragraph({ children: (!headless && r === 0) || (headless && c === 0) ? [new TextRun({ text: cell, bold: true, font: FONT })] : runs(cell) })],
      })),
    })),
  });
}

function block(b) {
  if (b.h2) return [new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun(b.h2)] })];
  if (b.h3) return [new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun(b.h3)] })];
  if (b.p) return [new Paragraph({ spacing: { after: 120 }, children: runs(b.p) })];
  if (b.ul) return b.ul.map((t) => new Paragraph({ numbering: { reference: "bullets", level: 0 }, children: runs(t) }));
  if (b.ol) {
    const ref = `numbers-${numberingRefs.length + 1}`;
    numberingRefs.push(ref);
    return b.ol.map((t) => new Paragraph({ numbering: { reference: ref, level: 0 }, children: runs(t) }));
  }
  if (b.table) return [table(b.table, b.widths, b.glance), new Paragraph({ spacing: { after: 60 }, children: [] })];
  if (b.code) return b.code.split("\n").map((line, i, all) => new Paragraph({
    shading: { fill: "F4F4F4", type: ShadingType.CLEAR, color: "auto" },
    spacing: { before: i === 0 ? 60 : 0, after: i === all.length - 1 ? 120 : 0 },
    children: [new TextRun({ text: line || " ", font: CODE, size: 19 })],
  }));
  throw new Error("Unknown block " + JSON.stringify(b));
}

// Adds the day's recall questions, new terms, definition of done and recall answers around the task text
function withExtras(blocks, extra) {
  const at = (prefix) => blocks.findIndex((b) => b.h2 && b.h2.startsWith(prefix));
  const out = [...blocks];
  out.splice(at("6."), 0,
    { p: "**Done when:** " + extra.done });
  out.splice(at("3."), 0,
    { p: "**New terms today:** " + extra.terms.join(", ") + ". All are defined in `reference/glossary.md`." });
  return [
    { h2: "Before you start (2 minutes, from memory)" },
    { p: "Answer these without looking. The effort of remembering is what makes it stick. Answers are at the end." },
    { ol: extra.recall.map(([q]) => q) },
    ...out,
    { h2: "Answers to \"Before you start\"" },
    { ol: extra.recall.map(([, a]) => a) },
  ];
}

// The facilitator's hint ladder, released one step at a time in the chat
function hintsText(brief, extra) {
  const lines = [
    `# Day ${String(brief.day).padStart(2, "0")} hints — ${brief.dayTitle}`,
    "",
    "For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.",
    "Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.",
    "",
  ];
  for (const { task, hints } of extra.hints) {
    lines.push(`## ${task}`, "");
    hints.forEach((h, i) => lines.push(`${i + 1}. ${h}`));
    lines.push("");
  }
  return lines.join("\n");
}

let numberingRefs = []; // one numbering definition per numbered list, so each list restarts at 1

// Renders one day; writes nothing. Returns { docx: Buffer, hints: string | null }.
async function renderDay(brief) {
  numberingRefs = [];
  const withFolder = JSON.parse(render(JSON.stringify(brief.blocks), brief.day));
  // The reference workshop brief opens with an at-a-glance table under the overview
  const firstPara = withFolder.findIndex((b) => b.p);
  let blocks = [...withFolder.slice(0, firstPara + 1), { table: [["", ""], ...brief.glance], widths: [3000, 6360], glance: true }, ...withFolder.slice(firstPara + 1)];
  const extra = extras[brief.day] && JSON.parse(render(JSON.stringify(extras[brief.day]), brief.day));
  if (extra) blocks = withExtras(blocks, extra);
  // First thing on the page, for everyone in the room, including people who don't write code
  blocks = [{ h2: "In plain English" }, { p: brief.plain }, ...blocks];
  const body = blocks.flatMap(block);
  const doc = new Document({
    creator: "Java upskilling",
    title: brief.title,
    styles: {
      default: { document: { run: { font: FONT, size: 22 } } },
      paragraphStyles: [
        { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
          run: { font: FONT, size: 36, bold: true, color: "1F3864" },
          paragraph: { spacing: { before: 0, after: 200 }, keepNext: true, outlineLevel: 0 } },
        { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
          run: { font: FONT, size: 28, bold: true, color: "1F3864" },
          paragraph: { spacing: { before: 280, after: 120 }, keepNext: true, outlineLevel: 1 } },
        { id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
          run: { font: FONT, size: 24, bold: true, color: "2F5496" },
          paragraph: { spacing: { before: 200, after: 80 }, keepNext: true, outlineLevel: 2 } },
      ],
    },
    numbering: {
      config: [
        { reference: "bullets", levels: [{ level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
        ...numberingRefs.map((reference) => ({ reference, levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.",
          alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] })),
      ],
    },
    sections: [{
      properties: { page: { size: { width: 12240, height: 15840 }, margin: { top: 1440, bottom: 1440, left: 1440, right: 1440 } } },
      children: [
        new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun(brief.title)] }),
        new Paragraph({ spacing: { after: 200 }, children: [new TextRun({ text: brief.subtitle, italics: true, color: "595959", font: FONT })] }),
        ...body,
      ],
    }],
  });
  return { docx: await Packer.toBuffer(doc), hints: extra ? hintsText(brief, extra) : null };
}

// The document's content: every XML part (body, styles, numbering, links), without what changes on every
// build: the zip's timestamps, docProps (creation time) and the random ids docx gives each hyperlink
async function documentXml(buffer) {
  const zip = await JSZip.loadAsync(buffer);
  const names = Object.keys(zip.files).filter((n) => n.endsWith(".xml") || n.endsWith(".rels")).filter((n) => !n.startsWith("docProps/")).sort();
  const parts = await Promise.all(names.map(async (n) => n + "\n" + (await zip.file(n).async("string"))));
  return parts.join("\n").replace(/rId[a-z0-9_-]{8,}/gi, "rId");
}

module.exports = { facts, render };

if (require.main === module) (async () => {
  const check = process.argv.includes("--check");
  const only = process.argv.slice(2).filter((a) => !a.startsWith("--")).map(Number);
  const stale = [];
  for (const entry of briefs) {
    if (only.length && !only.includes(entry.day)) continue;
    const brief = fromManifest(entry);
    const { docx, hints } = await renderDay(brief);
    const docxPath = path.join(ROOT, brief.folder, "brief.docx");
    const hintsPath = path.join(ROOT, brief.folder, "hints.md");
    if (check) {
      if (!fs.existsSync(docxPath) || (await documentXml(fs.readFileSync(docxPath))) !== (await documentXml(docx))) stale.push(docxPath);
      if (hints !== null && (!fs.existsSync(hintsPath) || fs.readFileSync(hintsPath, "utf8") !== hints)) stale.push(hintsPath);
    } else {
      fs.writeFileSync(docxPath, docx);
      if (hints !== null) fs.writeFileSync(hintsPath, hints);
      console.log("wrote", path.relative(ROOT, docxPath));
    }
  }
  stale.forEach((p) => console.log("out of date:", path.relative(ROOT, p)));
  process.exitCode = stale.length ? 1 : 0;
})();
