# Runs in the browser via Pyodide (Python -> WebAssembly). Output: key=value lines.
import csv, io, statistics as st

def analyze(text):
    rows = [r for r in csv.reader(io.StringIO(text)) if r]
    if len(rows) < 3 or len(rows[0]) < 2:
        return "type=free text (no tabular data detected)"
    head, body = rows[0], rows[1:]
    out = ["type=table", f"data rows={len(body)}", f"columns={len(head)}"]
    for i, name in enumerate(head):
        name = name.replace("=", "-").strip() or f"col{i+1}"
        vals = []
        for r in body:
            try: vals.append(float(r[i].replace(",", "")))
            except (ValueError, IndexError): pass
        if len(vals) < max(2, len(body) // 2):
            out.append(f"{name}=non-numeric column")
            continue
        m, sd = st.mean(vals), st.pstdev(vals)
        outliers = sum(1 for v in vals if sd and abs(v - m) > 3 * sd)
        out.append(f"{name}: mean / median={m:.4g} / {st.median(vals):.4g}")
        out.append(f"{name}: min / max={min(vals):.4g} / {max(vals):.4g}")
        out.append(f"{name}: std dev={sd:.4g}; outliers (>3 sigma)={outliers}")
        out.append(f"{name}: missing={len(body) - len(vals)}")
    return "\n".join(out)
