// C++ -> WebAssembly (Emscripten). Fast text metrics. Output: key=value lines.
#include <algorithm>
#include <cctype>
#include <cmath>
#include <map>
#include <string>
#include <vector>

static std::string out;

extern "C" const char* analyze_text(const char* s) {
    std::string text(s), w;
    std::map<std::string, int> freq;
    size_t words = 0, lines = 1, sentences = 0, counts[256] = {0};
    auto flush = [&] { if (!w.empty()) { freq[w]++; words++; w.clear(); } };
    for (unsigned char c : text) {
        counts[c]++;
        if (std::isalnum(c) || c == '\'') w += (char)std::tolower(c);
        else {
            flush();
            if (c == '\n') lines++;
            if (c == '.' || c == '!' || c == '?') sentences++;
        }
    }
    flush();
    double entropy = 0;
    for (size_t n : counts) if (n) { double p = (double)n / text.size(); entropy -= p * std::log2(p); }
    std::vector<std::pair<std::string, int>> top(freq.begin(), freq.end());
    std::sort(top.begin(), top.end(), [](auto& a, auto& b) { return a.second > b.second; });

    out = "characters=" + std::to_string(text.size()) + "\nwords=" + std::to_string(words) +
          "\nunique words=" + std::to_string(freq.size()) + "\nlines=" + std::to_string(lines) +
          "\nsentences=" + std::to_string(sentences) +
          "\navg chars per word=" + std::to_string(words ? (double)text.size() / words : 0.0) +
          "\nbyte entropy (bits)=" + std::to_string(entropy) + "\n";
    for (size_t i = 0; i < top.size() && i < 8; i++)
        out += "top word " + std::to_string(i + 1) + "=" + top[i].first + " (" + std::to_string(top[i].second) + ")\n";
    return out.c_str();
}
