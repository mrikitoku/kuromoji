/**
 * Copyright © 2010-2017 Atilika Inc. and contributors (see CONTRIBUTORS.md)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  A copy of the
 * License is distributed with this work in the LICENSE.md file.  You may
 * also obtain a copy of the License from
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atilika.kuromoji.ipadic;

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static com.atilika.kuromoji.TestUtils.assertTokenSurfacesEquals;
import static org.junit.Assert.assertEquals;

public class UserDictionaryTokenizerTest {

    private String userDictionary = "" +
        "クロ,クロ,クロ,カスタム名詞\n" +
        "真救世主,真救世主,シンキュウセイシュ,カスタム名詞\n" +
        "真救世主伝説,真救世主伝説,シンキュウセイシュデンセツ,カスタム名詞\n" +
        "北斗の拳,北斗の拳,ホクトノケン,カスタム名詞\n" +
        "成田国際空港,NRT,NRT,テスト名詞";

    @Test
    public void testBaseForm() throws IOException {
        String userDictionary = "NAIST,奈良先端科学技術大学院大学,naisuto,meisi";
        Tokenizer tokenizer = makeTokenizer(userDictionary);
        String input = "大学の略称はNAIST（ナイスト）、奈良先端大学";

        List<Token> tokens = tokenizer.tokenize(input);
        for (Token t : tokens) {
            System.out.println("----");
            System.out.println(" surface:" + t.getSurface() );
            System.out.println(" base form:" + t.getBaseForm() );
            System.out.println(" reading:" + t.getReading());
            System.out.println(" class:" + t.getPartOfSpeechLevel1());
            System.out.println(" " + t.getAllFeatures());
        }
    }

    @Test
    public void testWhitespace() throws IOException {
        String userDictionary = "iPhone4 S,iPhone4 S,iPhone4 S,カスタム名詞";
        Tokenizer tokenizer = makeTokenizer(userDictionary);
        String input = "iPhone4 S";

        assertTokenSurfacesEquals(
            Arrays.asList("iPhone4 S"),
            tokenizer.tokenize(input)
        );
    }

    @Test(expected = RuntimeException.class)
    public void testBadlyFormattedEntry() throws IOException {
        String entry = "関西国際空港,関西 国際 空,カンサイ コクサイクウコウ,カスタム名詞";
        makeTokenizer(entry);
    }

    @Test
    public void testAcropolis() throws IOException {
        String userDictionary = "クロ,クロ,クロ,カスタム名詞";
        Tokenizer tokenizer = makeTokenizer(userDictionary);

        String input = "アクロポリス";

        assertTokenSurfacesEquals(
            Arrays.asList("ア", "クロ", "ポリス"),
            tokenizer.tokenize(input)
        );
    }

    @Test
    public void testAllFeatures() throws IOException {
        String input = "シロクロ";
        String[] surfaces = {"シロ", "クロ"};
        Tokenizer tokenizer = makeTokenizer(userDictionary);
        List<Token> tokens = tokenizer.tokenize(input);

        assertEquals(surfaces.length, tokens.size());
        Token token = tokens.get(1);
        String actual = token.getSurface() + "\t" + token.getAllFeatures();
        assertEquals("クロ\tカスタム名詞,*,*,*,*,*,クロ,クロ,*", actual);
    }


    @Test
    public void testAcropolisInSentence() throws IOException {
        String userDictionary = "クロ,クロ,クロ,カスタム名詞";
        Tokenizer tokenizer = makeTokenizer(userDictionary);

        String input = "この丘はアクロポリスと呼ばれている。";

        assertTokenSurfacesEquals(
            Arrays.asList("この", "丘", "は", "ア", "クロ", "ポリス", "と", "呼ば", "れ", "て", "いる", "。"),
            tokenizer.tokenize(input)
        );
    }

    @Test
    public void testLatticeBrokenAfterUserDictEntry() throws IOException {
        String userDictionary = "クロ,クロ,クロ,カスタム名詞";
        Tokenizer tokenizer = makeTokenizer(userDictionary);

        String input = "アクロア";
        String[] surfaces = {"ア", "クロ", "ア"};
        String[] features = {
            "*,*,*,*,*,*,*,*,*",
            "カスタム名詞,*,*,*,*,*,クロ,クロ,*",
            "*,*,*,*,*,*,*,*,*"
        };
        List<Token> tokens = tokenizer.tokenize(input);

        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(surfaces[i], tokens.get(i).getSurface());
            assertEquals(features[i], tokens.get(i).getAllFeatures());
        }
    }

    @Test
    public void testLatticeBrokenAfterUserDictEntryInSentence() throws IOException {
        String userDictionary = "クロ,クロ,クロ,カスタム名詞";
        Tokenizer tokenizer = makeTokenizer(userDictionary);

        String input = "この丘の名前はアクロアだ。";
        String[] surfaces = {"この", "丘", "の", "名前", "は", "ア", "クロ", "ア", "だ", "。"};
        String[] features = {
            "連体詞,*,*,*,*,*,この,コノ,コノ",
            "名詞,一般,*,*,*,*,丘,オカ,オカ",
            "助詞,連体化,*,*,*,*,の,ノ,ノ",
            "名詞,一般,*,*,*,*,名前,ナマエ,ナマエ",
            "助詞,係助詞,*,*,*,*,は,ハ,ワ",
            "*,*,*,*,*,*,*,*,*",
            "カスタム名詞,*,*,*,*,*,クロ,クロ,*",
            "*,*,*,*,*,*,*,*,*",
            "助動詞,*,*,*,特殊・ダ,基本形,だ,ダ,ダ",
            "記号,句点,*,*,*,*,。,。,。"
        };
        List<Token> tokens = tokenizer.tokenize(input);

        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(surfaces[i], tokens.get(i).getSurface());
            assertEquals(features[i], tokens.get(i).getAllFeatures());
        }
    }

    @Test
    public void testShinKyuseishu() throws IOException {
        String userDictionary = "真救世主,真救世主,シンキュウセイシュ,カスタム名詞";
        Tokenizer tokenizer = makeTokenizer(userDictionary);

        assertEquals("シンキュウセイシュ", tokenizer.tokenize("真救世主伝説").get(0).getReading());
    }

    @Test
    public void testShinKyuseishuDensetsu() throws IOException {
        String userDictionary = "真救世主伝説,真救世主伝説,シンキュウセイシュデンセツ,カスタム名詞";
        Tokenizer tokenizer = makeTokenizer(userDictionary);

        assertEquals("シンキュウセイシュデンセツ", tokenizer.tokenize("真救世主伝説").get(0).getReading());
    }

    @Test
    public void testCheckDifferentSpelling() throws IOException {
        String input = "北斗の拳は真救世主伝説の名曲である。";
        Tokenizer tokenizer = makeTokenizer(userDictionary);
        List<Token> tokens = tokenizer.tokenize(input);
        String[] expectedReadings = {"ホクトノケン", "ハ", "シンキュウセイシュデンセツ", "ノ", "メイキョク", "デ", "アル", "。"};

        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(expectedReadings[i], tokens.get(i).getReading());
        }
    }

    @Test
    public void testLongestActualJapaneseWord() throws IOException {
        String userDictionary = "竜宮の乙姫の元結の切り外し,竜宮の乙姫の元結の切り外し,リュウグウノオトヒメノモトユイノキリハズシ,カスタム名詞";
        Tokenizer tokenizer = makeTokenizer(userDictionary);

        assertEquals(
            "リュウグウノオトヒメノモトユイノキリハズシ",
            tokenizer.tokenize("竜宮の乙姫の元結の切り外し").get(0).getReading()
        );
    }

    @Test
    public void testLongestMovieTitle() throws IOException {
        String userDictionary = "マルキ・ド・サドの演出のもとにシャラントン精神病院患者たちによって演じられたジャン＝ポール・マラーの迫害と暗殺,"
            + "マルキ・ド・サドの演出のもとにシャラントン精神病院患者たちによって演じられたジャン＝ポール・マラーの迫害と暗殺,"
            + "マルキ・ド・サドノエンシュツノモトニシャラントンセイシンビョウインカンジャタチニヨッテエンジラレタジャン＝ポール・マラーノハクガイトアンサツ,"
            + "カスタム名詞";
        Tokenizer tokenizer = makeTokenizer(userDictionary);

        assertEquals(
            "マルキ・ド・サドノエンシュツノモトニシャラントンセイシンビョウインカンジャタチニヨッテエンジラレタジャン＝ポール・マラーノハクガイトアンサツ",
            tokenizer.tokenize("マルキ・ド・サドの演出のもとにシャラントン精神病院患者たちによって演じられたジャン＝ポール・マラーの迫害と暗殺").get(0).getReading()
        );
    }

    @Test
    public void testOverlappingUserEntries() throws Exception {
        String userDictionary = "クリ,クリ,クリ,カスタム名詞\n" +
            "チャン,チャン,チャン,カスタム名詞\n" +
            "リスチャン,リスチャン,リスチャン,カスタム名詞";

        String input = "クリスチャンは寿司が大好きです。";

        Tokenizer tokenizer = makeTokenizer(userDictionary);

        List<Token> tokens = tokenizer.tokenize(input);

        assertTokenSurfacesEquals(
            Arrays.asList("ク", "リスチャン", "は", "寿司", "が", "大好き", "です", "。"),
            tokens
        );
    }

    @Test
    public void testShorterEntryMatchWhenUserEntriesOverlap() throws Exception {
        String userDictionary = "関西国際空港,関西国際空港,かんさいこくさいくうこう,カスタム施設\n" +
            "関西,関西,かんさい,カスタム地名";

        String input = "関西国際医療センター";

        Tokenizer tokenizer = makeTokenizer(userDictionary);

        List<Token> tokens = tokenizer.tokenize(input);

        assertEquals("関西", tokens.get(0).getSurface());
        assertEquals("カスタム地名", tokens.get(0).getPartOfSpeechLevel1());
    }

    @Test
    public void testInsertedFail() throws IOException {
        String userDictionary = "引,引,引,カスタム品詞\n";
        Tokenizer tokenizer = makeTokenizer(userDictionary);

        assertTokenSurfacesEquals(
            Arrays.asList("引", "く", "。"),
            tokenizer.tokenize("引く。")
        );
    }

    @Test
    public void testFullUserDictionary() throws IOException {
        String userDictionary = "" +
            "日本経済新聞,日本 経済 新聞,ニホン ケイザイ シンブン,カスタム名詞\n" +
            "渡部,1290,1290,5900,カスタム名詞,固有名詞,人名,姓,*,*,渡部,ワタナベ,ワタナベ\n";

        Tokenizer tokenizer = makeTokenizer(userDictionary);

        String input = "渡部さんは日本経済新聞社に勤めている。";
        String[] surfaces = {"渡部", "さん", "は", "日本", "経済", "新聞", "社", "に", "勤め", "て", "いる", "。"};
        String[] features = {
            "カスタム名詞,固有名詞,人名,姓,*,*,渡部,ワタナベ,ワタナベ",
            "名詞,接尾,人名,*,*,*,さん,サン,サン",
            "助詞,係助詞,*,*,*,*,は,ハ,ワ",
            "カスタム名詞,*,*,*,*,*,日本,ニホン,*",
            "カスタム名詞,*,*,*,*,*,経済,ケイザイ,*",
            "カスタム名詞,*,*,*,*,*,新聞,シンブン,*",
            "名詞,一般,*,*,*,*,社,シャ,シャ",
            "助詞,格助詞,一般,*,*,*,に,ニ,ニ",
            "動詞,自立,*,*,一段,連用形,勤める,ツトメ,ツトメ",
            "助詞,接続助詞,*,*,*,*,て,テ,テ",
            "動詞,非自立,*,*,一段,基本形,いる,イル,イル",
            "記号,句点,*,*,*,*,。,。,。"
        };
        List<Token> tokens = tokenizer.tokenize(input);

        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(surfaces[i], tokens.get(i).getSurface());
            assertEquals(features[i], tokens.get(i).getAllFeatures());
        }
    }

    @Ignore("Doesn't segment properly - Viterbi lattice looks funny")
    @Test
    public void testTsunk() throws IOException {
        String userDictionary = "" +
            "シャ乱Q つんく♂,シャ乱Q つんく ♂,シャランキュー ツンク ボーイ,カスタムアーティスト名";
        Tokenizer tokenizer = makeTokenizer(userDictionary);

        FileOutputStream output = new FileOutputStream("tsunk.gv");
        tokenizer.debugTokenize(output, "シャQ");
        output.close();
    }

    private Tokenizer makeTokenizer(String userDictionaryEntry) throws IOException {
        return new Tokenizer.Builder()
            .userDictionary(
                makeUserDictionaryStream(userDictionaryEntry)
            )
            .build();
    }

    private ByteArrayInputStream makeUserDictionaryStream(String userDictionary) {
        return new ByteArrayInputStream(
            userDictionary.getBytes(StandardCharsets.UTF_8)
        );
    }
}
