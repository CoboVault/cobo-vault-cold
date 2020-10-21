/*
 *
 *  Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cobo.coinlib.coins.XRP.xumm;

public interface Schemas {
    String TrustSet = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LimitAmount\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"properties\": {\n" +
            "        \"currency\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"issuer\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"value\": {\n" +
            "          \"type\": \"string\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"required\": [\n" +
            "        \"currency\",\n" +
            "        \"issuer\",\n" +
            "        \"value\"\n" +
            "      ]\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"TransactionType\",\n" +
            "    \"Account\",\n" +
            "    \"Fee\",\n" +
            "    \"Flags\",\n" +
            "    \"LimitAmount\",\n" +
            "    \"Sequence\"\n" +
            "  ]\n" +
            "}";
}
