You are tasked with generating fine-tuning data for a machine learning model that interacts through a conversation format.
Given the passage below, your goal is to create an array of a structured conversations that includes the user's query and the assistant's response.
The array should contain {numberOfConversation} conversations.

Text passage :
"{textPassage}"

Instructions:

Language : Use the language of the provided text passage to generate the different messages (user and assistant)

User Message: Generate a relevant and clear question based on the provided text. This represents the user's query.

Assistant Message: Provide a concise answer to the user's question using only the information available in the text passage. The response should be accurate and brief, typically one or two sentences.

Format in JSON: Output the conversation as a valid array of JSON object with a messages array that includes system, user, and assistant roles. Don't add any text before of after the json output.
JSON Format:

[\{
  "messages": [
    \{
      "role": "user",
      "content": "the Generated Question"
    \},
    \{
      "role": "assistant",
      "content": "the Generated Response"
    \}
  ]
\}]

Example:
Text passage :
"Fly Intelligent, fondée en 2022, est une compagnie aérienne dédiée à la promotion du voyage aérien durable. Son
 réseau actuel comprend des destinations suivantes: Maroua, Le Cap, Londres et Sydney."

Generated JSON Conversation:

[\{
  "messages": [
    \{
      "role": "user",
      "content": "Quelles sont les destinations assurées par Fly Intelligent ?"
    \},
    \{
      "role": "assistant",
      "content": "Fly Intelligent dessert actuellement les destinations suivantes : Maroua, Le Cap, Londres et Sydney"
    \}
  ]
\},
\{
  "messages": [
    \{
      "role": "user",
      "content": "Qu'est-ce que Fly Intellingent ?"
    \},
    \{
      "role": "assistant",
      "content": "Fly Intelligent est une compagnie aérienne dédiée à la promotion du voyage aérien durable."
    \}
  ]
\}]
