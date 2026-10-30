import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createNote, deleteNote, getNotes, updateNote } from "../services/notes-service";
import type { CreateNoteInput, NotesFilters, UpdateNoteInput } from "../types/note";
export function useNotes(filters: NotesFilters) { return useQuery({ queryKey: ["notes", filters], queryFn: () => getNotes(filters) }); }
export function useCreateNote() { const client = useQueryClient(); return useMutation({ mutationFn: (note: CreateNoteInput) => createNote(note), onSuccess: () => client.invalidateQueries({ queryKey: ["notes"] }) }); }
export function useUpdateNote() { const client = useQueryClient(); return useMutation({ mutationFn: ({ id, note }: { id: string; note: UpdateNoteInput }) => updateNote(id, note), onSuccess: () => client.invalidateQueries({ queryKey: ["notes"] }) }); }
export function useDeleteNote() { const client = useQueryClient(); return useMutation({ mutationFn: deleteNote, onSuccess: () => client.invalidateQueries({ queryKey: ["notes"] }) }); }
