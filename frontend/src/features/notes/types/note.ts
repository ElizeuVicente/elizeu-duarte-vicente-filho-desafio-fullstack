export type Note = { id: string; site: string; equipment: string; variable: string; timestamp: string; author: string; message: string };
export type NotesPage = { content: Note[]; totalElements: number; totalPages: number; size: number; number: number; first: boolean; last: boolean; empty: boolean };
export type NotesFilters = { site: string; equipment: string; startDate: string; endDate: string; page: number; size: number };
export type CreateNoteInput = Omit<Note, "id">;
export type UpdateNoteInput = CreateNoteInput;
