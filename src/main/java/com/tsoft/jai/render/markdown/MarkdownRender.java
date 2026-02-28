package com.tsoft.jai.render.markdown;

import com.tsoft.jai.utils.base.Triple;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Arrays;

@Data
@Accessors(chain = true)
public class MarkdownRender {

    private RenderOptions options;
    // private syntax_set: SyntaxSet,
    // private Color code_color: Option<Color>,
    // private md_syntax: SyntaxReference,
    // private code_syntax: Option<SyntaxReference>,
    // private prev_line_type: LineType,
    // private wrap_width: Option<u16>,

    // pub fn init(options: RenderOptions) -> Result<Self> {
    //    let syntax_set: SyntaxSet =
    //        decode_bin(SYNTAXES).with_context(|| "MarkdownRender: invalid syntaxes binary")?;
    //
    //    let code_color = options
    //        .theme
    //        .as_ref()
    //        .map(|theme| get_code_color(theme, options.truecolor));
    //    let md_syntax = syntax_set.find_syntax_by_extension("md").unwrap().clone();
    //    let line_type = LineType::Normal;
    //    let wrap_width = match options.wrap.as_deref() {
    //        None => None,
    //        Some(value) => match terminal::size() {
    //            Ok((columns, _)) => {
    //                if value == "auto" {
    //                    Some(columns)
    //                } else {
    //                    let value = value
    //                        .parse::<u16>()
    //                        .map_err(|_| anyhow!("Invalid wrap value"))?;
    //                    Some(columns.min(value))
    //                }
    //            }
    //            Err(_) => None,
    //        },
    //    };
    //    Ok(Self {
    //        syntax_set,
    //        code_color,
    //        md_syntax,
    //        code_syntax: None,
    //        prev_line_type: line_type,
    //        wrap_width,
    //        options,
    //    })
    // }
    public static MarkdownRender init(RenderOptions options) {
        return new MarkdownRender();
    }

    // pub fn render(&mut self, text: &str) -> String {
    //     text.split('\n')
    //         .map(|line| self.render_line_mut(line))
    //         .collect::<Vec<String>>()
    //         .join("\n")
    // }
    public String render(String text) {
        return String.join("\n", Arrays.stream(text.split("\n"))
            .map(line -> renderLineMut(line))
            .toList());
    }

    // pub fn render_line(&self, line: &str) -> String {
    //    let (_, code_syntax, is_code) = self.check_line(line);
    //    if is_code {
    //        self.highlight_code_line(line, &code_syntax)
    //    } else {
    //        self.highlight_line(line, &self.md_syntax, false)
    //    }
    // }
    public String renderLine(String line) {
        return line;
    }

    // fn render_line_mut(&mut self, line: &str) -> String {
    //    let (line_type, code_syntax, is_code) = self.check_line(line);
    //    let output = if is_code {
    //        self.highlight_code_line(line, &code_syntax)
    //    } else {
    //        self.highlight_line(line, &self.md_syntax, false)
    //    };
    //    self.prev_line_type = line_type;
    //    self.code_syntax = code_syntax;
    //    output
    // }
    public String renderLineMut(String line) {
        return null;
    }
}
