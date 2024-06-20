<!-- 
This file is part of BarrierFreeNav.
 *
 * Copyright (c) 2024 에드윈
 *
 * Licensed under the CC BY 4.0 License (https://creativecommons.org/licenses/by/4.0/legalcode). -->

import speech_recognition as sr

# 음성 인식 객체 생성
recognizer = sr.Recognizer()

# 마이크에서 입력받기
with sr.Microphone() as source:
    print("말하세요...")
    audio = recognizer.listen(source)

# Google Web Speech API를 사용하여 음성 인식
try:
    print("인식된 텍스트: " + recognizer.recognize_google(audio, language='ko-KR'))
except sr.UnknownValueError:
    print("Google Web Speech API가 당신의 말을 이해하지 못했습니다.")
except sr.RequestError as e:
    print(f"Google Web Speech API 서비스에 문제가 발생했습니다; {e}")
