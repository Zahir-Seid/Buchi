package com.buchi.petfinder.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private String age;

    @Column(name = "good_with_children", nullable = false)
    private Boolean goodWithChildren;

    @ElementCollection
    @CollectionTable(name = "pet_photos",
            joinColumns = @JoinColumn(name = "pet_id"))
    @Column(name = "photo_url")
    @Builder.Default
    private List<String> photos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
