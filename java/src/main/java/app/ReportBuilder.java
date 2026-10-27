package app;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/** Java -> browser (TeaVM). Turns analysis results into a standalone HTML report. */
public class ReportBuilder {
    @JSFunctor
    public interface Renderer extends JSObject {
        String render(String title, String cpp, String py, String when);
    }

    @JSBody(params = {"f"}, script = "window.javaRender = f;")
    static native void expose(Renderer f);

    public static void main(String[] args) { expose(ReportBuilder::build); }

    static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    static String section(String heading, String lines) {
        if (lines == null || lines.trim().isEmpty()) return "";
        StringBuilder sb = new StringBuilder("<h2>" + esc(heading) + "</h2><table>");
        for (String line : lines.split("\n")) {
            int i = line.indexOf('=');
            if (i < 0) continue;
            sb.append("<tr><th>").append(esc(line.substring(0, i))).append("</th><td>")
              .append(esc(line.substring(i + 1))).append("</td></tr>");
        }
        return sb.append("</table>").toString();
    }

    static String build(String title, String cpp, String py, String when) {
        return "<!doctype html><html><head><meta charset=utf-8><title>" + esc(title) + "</title><style>"
            + "body{font:16px system-ui;max-width:820px;margin:2rem auto;padding:0 1rem;color:#222}"
            + "h1{border-bottom:3px solid #2b6cb0;padding-bottom:.4rem}h2{margin-top:2rem;color:#2b6cb0}"
            + "table{width:100%;border-collapse:collapse}th,td{padding:.45rem .6rem;border-bottom:1px solid #ddd;text-align:left}"
            + "th{width:40%;font-weight:600}small{color:#666}</style></head><body><h1>" + esc(title) + "</h1>"
            + "<small>Generated " + esc(when) + " - entirely in your browser</small>"
            + section("Text metrics (C++ / WebAssembly)", cpp)
            + section("Data analysis (Python / Pyodide)", py)
            + "</body></html>";
    }
}
