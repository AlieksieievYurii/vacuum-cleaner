int32_t find_character_index(char* input, char character, uint8_t limit) {
  for (uint32_t i = 0; i <= limit; i++) {
    if (input[i] == character)
      return i;
  }
  return -1;
}
