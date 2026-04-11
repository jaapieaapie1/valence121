use std::collections::HashMap;

use heck::ToShoutySnakeCase;
use proc_macro2::TokenStream;
use quote::quote;
use serde::Deserialize;
use valence_build_utils::{ident, rerun_if_changed, write_generated_file};

pub fn main() -> anyhow::Result<()> {
    write_generated_file(build()?, "translation_keys.rs")
}

fn build() -> anyhow::Result<TokenStream> {
    rerun_if_changed(["extracted/translation_keys.json"]);

    let mut translations =
        serde_json::from_str::<Vec<Translation>>(include_str!("extracted/translation_keys.json"))?;

    // Sort by key so collision disambiguation is deterministic across extractor runs.
    translations.sort_by(|a, b| a.key.cmp(&b.key));

    // First pass: count how many translation keys map to each shouty-snake-case name so
    // we can disambiguate collisions (e.g. "book.editTitle" and "book.edit.title" both
    // become BOOK_EDIT_TITLE).
    let mut name_counts: HashMap<String, usize> = HashMap::new();
    for translation in &translations {
        *name_counts
            .entry(translation.key.to_shouty_snake_case())
            .or_default() += 1;
    }

    let mut name_seen: HashMap<String, usize> = HashMap::new();
    let translation_key_consts = translations
        .iter()
        .map(|translation| {
            let base_name = translation.key.to_shouty_snake_case();
            let name = if name_counts[&base_name] > 1 {
                let idx = name_seen.entry(base_name.clone()).or_insert(0);
                *idx += 1;
                if *idx == 1 {
                    base_name
                } else {
                    format!("{base_name}_{idx}")
                }
            } else {
                base_name
            };
            let const_id = ident(name);
            let key = &translation.key;
            let english_translation = &translation.english_translation;
            let doc = format!("\"{}\"", escape(english_translation)).replace('`', "\\`");

            quote! {
                #[doc = #doc]
                pub const #const_id: &str = #key;
            }
        })
        .collect::<Vec<TokenStream>>();

    Ok(quote! {
        #(#translation_key_consts)*
    })
}

#[derive(Deserialize, Clone, Debug)]
struct Translation {
    key: String,
    english_translation: String,
}

/// Escapes characters that have special meaning inside docs.
fn escape(text: &str) -> String {
    text.replace('[', "\\[").replace(']', "\\]")
}
